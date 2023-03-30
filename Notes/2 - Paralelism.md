# Paralelismo

## Lei de Amdahl

A performance da aplicação está limitada à sua parte sequencial, onde não dá para introduzir o paralelismo. Segundo a Lei de Amdahl:

```note
speedup = 1 / (p/P + s)
    - s: parte sequencial
    - p: parte paralela
    - P: número de processadores
    - 1 = p + s
```

Assim, para um limite muito grande de processadores, o *speedup* está limitado a 1/s, ou seja, à percentagem da componente sequencial do código programado. <br>
Esta perspectiva é otimista, pois com muitos processadores é necessário haver controlo de:
- conflitos em acessos de recursos, como memória e rede;
- ciclos de processamento que suportam o paralelismo;
- ciclos de processamento para a sincronização entre processos;

## Tipos de Paralelismo

### Paralelismo Funcional

Quando tarefas independentes executam diferentes operações em diferentes conjuntos de dados.

```python
a = 2
b = 3
m = (a + b) / 2
s = (a^2 + b^2) / 2
v = s - m^2
```

Neste caso as instruções 1 e 2 são independentes, mas as seguintes são dependentes das anteriores. Neste caso o grafo de dependências entre instruções é acíclico e direto (um *DAG*), onde as arestas representam as dependências funcionais.

Exemplo: <br>
Soma dos elementos de um vetor:

```python
sum = 0
for element in vector:
    sum += element
```

Nesta situação existe um paralelismo funcional, onde as sequências das somas pode ser efetuadas de par a par, reduzindo o tempo e transformando o ciclo em algo mais eficiente.

### Paralelismo de Dados

Quando diferentes tarefas executam a mesma operação consultando dados diferentes. É o exemplo que acontece no produto de matrizes. Exemplo:

```c
for (int i = 0 ; i < 99 ; i++) {
    a[i] = b[i] + c[i];
}
```

É possível implementar a soma dos dois vetores simultaneamente, dividindo-os em blocos e tratando cada um antes de juntar à solução final.

### Streaming

Divide o processamento dos dados em partes e cada uma fica da responsabilidade do núcleo. O número de passos limita o *speedup* do programa.

Exemplos:
- Real time data analysis;
- Real time making support;

## Modelos de Programação Paralela

### Modelo de Memória Distribuída

Idealmente para programas com tarefas sequenciais. A mesma memória local não está acessível a todos os cors ou máquinas da mesma forma, mas existem canais de comunicações.

- Partição do problema em subprocessos;
- Gerir o canal de comunicações, para juntar as subsoluções;
- Aglomerar os resultados obtidos. Depende do número de cors da máquina usada;
- Mapear o resultado final;

### Modelo de Memória Partilhada

A mesma memória é partilhada por todas as máquinas intervenientes nos cálculos. <br>

O exemplo mais comum é o do **fork/join** em threads. O processo principal contém uma área de variáveis globais, e cada subprocesso filho contém variáveis locais bem como os seus próprios *program counter* e *stack pointer*.

Sempre que temos ciclos, *for loops*, é um ponto indicado para tratar com este tipo de paralelismo. Em C, usando a diretiva pragma com o OpenMP, podemos implementar assim:

```c
#pragma omp parallel for num_threads(k)
for (i = 0; i < N; i++)
    a[i] = b[i] + c[i];
```

Há criação de K novas threads, e cada uma processa N/K ciclos. O código torna-se mais simples de perceber em vez de usar as tradicionais **p-threads**.

Exemplo: <br>
Cálculo do valor de Pi. Um exemplo de solução não admissível:

```cpp
double area = 0.0, pi, x;
int n; // define a precisão do valor de pi calculado

#pragma omp parallel for private(x)
for (int i = 0 ; i < n ; i++) {
    x = (i + 0.5) / n;
    #pragma omp critical
    area += 4.0 / (1.0 + x * x);
}
pi = area / n;
```

Estamos a tornar privado o termo X para que cada thread criado tenha uma cópia do seu valor. Caso contrário teríamos um problema de **race condition**: não haver sobreposição de valores. 

Pelo contrário, o termo Area não pode ser privada, pois esse acumulador tem de ser conhecido de todas as `threads`. Esta variável tem de ter um **semáforo**, para que só um dos processos consiga escrever, mantendo o sincronismo e fazendo com que os outros processos esperem numa fila pela escrita. Como resultado, o tempo de processamento aumenta.

Outra solução é criar um array para os valores da área, para que existam mais acumuladores:

```cpp
double area[2], pi = 0.0, x;
int n;

for (int i = 0 ; i < 2 ; i++) {
    area[i] = 0.0;
}

#pragma omp parallel for private(x)
for (int i = 0 ; i < n ; i++) {
    x = (i+0.5) / n;
    area[omp_get_thread_num()] += 4.0/(1.0 + x*x);
}

for (int i = 0 ; i < 2 ; i++) {
    pi += area[i];
}
pi = pi / n;
```

Esta solução também não é admissível pois o array de área está contido no mesmo bloco de memória cache: `false sharing`. Assim cada processo estará a invalidar o bloco de memória do outro, e o esforço necessário para manter a consistência degrada a performance. 

Uma forma de contornar o `false sharing` é espaçar a memória do array de acordo com o tamanho do bloco de cache, para não haver invalidação em cada escrita da thread vizinha:

```cpp
double area = 0.0, pi, x;
int n;

#pragma omp parallel for private(x) reduction(+:area)
for (int i = 0 ; i < n ; i++) {
    x = (i + 0.5) / n;
    area += 4.0 / (1.0 + x*x);
}
pi = area / n;
```

### Modelo de MapReduce

Modelo mais recente que é usado para processar muitos dados. Faz operações de cálculo distribuído (*map*) em cada máquina e o (*reduce*) final para recolher os dados parciais calculados. Usado em *business inteligence*.

A operação de `Map` é característica de paralelismo de dados, enquanto que o `Reduce` é característica de uma operação de paralelismo funcional.

## Modelos de Memória

Por um lado temos o *Shared Memory*, onde o número de threads ativas é uma no início e uma no fim mas pode mudar dinamicamente durante a execução. Por outro temos o *Message Passing*, onde todos os processos ativos executam o programa e a transformação sequencial para paralela requer um maior esforço. 

### Modelo de memória partilhada

Quando uma máquina possui uma memória partilhada comum a todos os cors podemos usar diretivas de `Shared Memory` presente no OpenMP. Todas as variáveis declaradas na memória partilhada são visíveis na memória privada de cada cor. Se os cors quiserem manipular variáveis da memória partilhada:
- Declara a variável como privada (*private(variable)*), cria uma cópia para a sua própria memória;
- Declara a variável como zona crítica (*critical*), se o código que a utiliza for extenso, para criação de semáforos de acesso e modificação da mesma em modo concorrente;
- Declara a operação como atómica (*atomic*), se o código que a utiliza for pequeno;

Cada `thread` tem o seu próprio contexto de execução, que contém:
- variáveis estáticas;
- variáveis alocadas dinamicamente na heap;
- variáveis em runtime na stack;
- stack para as funções invocadas durante a execução da thread;