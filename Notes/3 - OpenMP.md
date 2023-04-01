# OpenMP

As diretivas só funcionam para a instrução seguinte e não para um conjunto de instruções, como por exemplo ciclos dentro de ciclos. <br>
Em ciclos *for*, há duas formas de garantir a integridade dos loops e das operações internas:

- Declarar a variável J como privada, há criação de uma cópia para cada thread criada:

```c
#pragma omp parallel for private(j)
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

A diretiva `firstprivate` cria variáveis privadas tendo valores iniciais idênticos à variável controlada pela thread mestre quando o loop é inicializado. Por default, `private` não define valores das variáveis, pelo que podem conter "lixo" da memória:

```c
int a = -1;
#pragma omp parallel private(a)
printf("Inside parallel:  Value of a = %d", a); // a = 37613, 7, 0...
printf("Outside parallel: Value of a = %d", a); // a = -1
```

A diretiva `lastprivate` passa para fora o último valor a executar/calcular no loop calculado pela última thread. Neste seguinte caso, o valor de **a** será 5 (número da última thread, 4, mais 1):

```c
int a, n=5;
#pragma omp parallel for lastprivate(a)
for (i=0; i<n; i++){ 
    a=i+1;
    printf("Thread %d has value a=%d for i=%d\n", omp_get_thread_num(), a, i);
}
printf("value after loop a=%d", a);
```

A diretiva `reduction` permite fazer *join* dos valores parciais de cada thread e retornar o valor para a memória partilhada, ou seja, tornar o resultado público após todas as iterações. É muito mais eficiente do que criar uma `critical section`. Exemplo de utilização:

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
- **#pragma omp master**, apenas o primeiro thread criado (o master) é que realiza a tarefa;
- **#pragma omp single**, o primeiro thread que chegar a esse ponto irá realizar a tarefa;
- **#pragma omp barrier**, garante que os threads criados fiquem sincronizados a partir desse ponto;

O `schedule` permite especificar quantos loops serão distribuídos pelas threads. Há três formas possíveis:
- **static**, quando as iterações são alocadas antes de todo o processamento, numa distribuição uniforme do trabalho. Tem pouco overhead;
- **dynamic**, quando só algumas iterações são alocadas e as restantes são escolhidas em runtime, para equilibrar o trabalho. Tem muito overhead e só compensa quando as threads não vão realizar o mesmo esforço / tempo de processamento;
- **guided**, quando a distribuição do loop segue uma heurística. Normalmente a alocação é maior no início e tende a ser menor no fim das execuções;

Podem ser indicados `chunks`, que são intervalos contínuos de iterações para cada thread. Aumentar o chunk reduz o overhead e pode aumentar o número de caches hit; diminuir o chunk permite balancear melhor a quantidade de trabalho entre threads.

## SPMD - Single Program Multiple Data

Abordagem mais flexível que permite não realizar a mesma instrução no mesmo momento, ao contrário do SIMD (Single Instruction Multiple Dat, como observado nas instruções assembly). Para este caso:

- `omp_get_num_threads`, retorna o número de threads do processador;
- `omp_get_thread_num`, retorna o id da thread, variando entre 0 e o número máximo que o processador suporta;

## Paralelismo Funcional

Por vezes o código tem várias dependências pelo que o paralelismo funciona só para algumas partes. No seguinte caso é possível paralelisar o cálculo de V e W e depois de X e Y, por esta ordem:

```c
v = alpha();
w = beta();
x = gamma(v, w);
y = delta();
printf ("%6.2f\n", epsilon(x,y));
```