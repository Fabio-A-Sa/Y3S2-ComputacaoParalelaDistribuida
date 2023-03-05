# Shared Memory

## Modelos de Memória

Por um lado temos o *Shared Memory*, onde o número de threads ativas é uma no início e uma no fim mas pode mudar dinamicamente durante a execução. Por outro temos o *Message Passing*, onde todos os processos ativos executam o programa e a transformação sequencial para paralela requer um maior esforço. 

### Modelo de memória partilhada

Quando uma máquina possui uma memória partilhada comum a todos os cors podemos usar diretivas de `Shared Memory` presente no OpenMP. Todas as variáveis declaradas na memória partilhada são visíveis na memória privada de cada cor. Se os cors quiserem manipular variáveis da memória partilhada:
- Declara a variável como privada (*private(variable)*), cria uma cópia para a sua própria memória;
- Declara a variável como zona crítica (*critical*), se o código que a utiliza for extenso, para criação de semáforos de acesso e modificação da mesma em modo concorrente;
- Declara a operação como atómica (*atomic*), se o cídigo que a utiliza for pequeno;

Cada `thread` tem o seu próprio contexto de execução, que contém:
- variáveis estáticas;
- variáveis alocadas dinamicamente na heap;
- variáveis em runtime na stack;
- stack para as funções invocadas durante a execução da thread;

## OpenMP

As diretivas só funcionam para a instrução seguinte e não para um conjunto de instruções, como por exemplo ciclos dentro de ciclos. <br>
Em ciclos *for*, há duas formas de garantir a integridade dos loops e das operações internas:

- Declarar a variável J como privada, há criação de uma cópia para cada thread criada:

```c
#pragma omp parallel for  private(j)
int i;
for (i = 0; i < n ; i++)
    for (j = 0 ; j < n ; j++)
        // do something 
```

- Definir a variável J dentro de cada thread:

```c
#pragma omp parallel for
int i;
for (i = 0; i < n ; i++)
    for (int j = 0 ; j < n ; j++)
        // do something 
```

Para encontrar o número de processos físicos no computador que podem ser usados em programa paralelo é necessário chamar uma função do sistema. Da mesma forma, dá para declarar o número de threads que queremos na diretiva (há duas formas, A ou B):

```c
int omp_get_num_procs(void);
void omp_set_num_threads(int num_threads); // A
#pragma omp parallel [num_threads(n)];     // B
```

A diretiva `firstprivate` cria variáveis privadas tendo valores iniciais idênticos à variável controlada pela thread mestre quando o loop é inicializado.

A diretiva `lastprivate` passa para fora o último valor a executar/calcular no loop calculado pela última thread.

A diretiva `reduction` permite fazer *join* dos valores parciais de cada thread e retornar o valor para a memória partilhada, ou seja, tornar o resultado público após todas as iterações. Exemplo de utilização:

```c
double area = 0.0, pi, x;
int i, n;

#pragma omp parallel for private(x) reduction(+:area)
for (i = 0 ; i < n ; i++) {
    x = (i + 0.5) / n;
    area += 4.0 / (1.0 + x*x);
}
pi = area / n;
```

A diretiva `nowait`, permite que os threads não sincronizem no final de cada ciclo e avancem para o bloco seguinte, em vez de estarem à espera que todos terminem.

Diretivas para blocos executados por uma thread única:
- **#pragma omp master**, apenas o primeiro thread criado é que realiza a tarefa;
- **#pragma omp single**, o primeiro thread que chegar a esse ponto irá realizar a tarefa;
- **#pragma omp barrier**, garante que os threads criados fiquem sincronizados a partir desse ponto;