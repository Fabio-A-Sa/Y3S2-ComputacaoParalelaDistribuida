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

A mesma memória não está acessível a todos os cors ou máquinas da mesma forma.

### Modelo de Memória Partilhada

A mesma memória é partilhada por todas as máquinas intervenientes nos cálculos.

### Modelo de MapReduce

Modelo mais recente que é usado para processar muitos dados. Faz operações de cálculo distribuído (*map*) em cada máquina e o (*reduce*) final para recolher os dados parciais calculados. Usado em *business inteligence*.
