# Introdução

## Computação Paralela

Surgiu da necessidade de termos mais núcleos do processador a executar o mesmo processo para aumentar a rapidez. Aumentar o número de ciclos de relógio (frequência) não é viável pois gasta demasiada energia. Os limites estáveis máximos estão na gama dos 4GHz e 140w de potência. 

Um compilador por si só não é capaz de transformar um programa sequencial num programa multicore, devido a manipulação de memória e à concorrência. Assim, os maiores desafios são a construção de programas escaláveis que continuem eficientes se:
- houver aumento substancial dos dados a manipular;
- existirem mais núcleos disponíveis;

Nesta unidade curricular o [OpenMP](https://www.openmp.org/) vai ser utilizado com diretivas que, em conjunto com o compilador habitual de C/C++, possam traduzir programas sequenciais em programas multicore.

## Performance

- `MIPS`: número de instruções por segundo, geralmente relacionado com a frequência do CPU. Tem em consideração todos os processos a calcular;
- `FLOPS`: número de instruções em vírgula flutuante por segundo, avaliado para cada processo;

Estes dois indicadores não são suficientes para classificar a performance do sistema. É necessário que essa performance seja sustentável, ou seja, que dependa o menos possível de factores como a velocidade I/O, hierarquia de memória ou acessos à mesma. Muitas vezes a performance depende do desenho do algoritmo implementado:

```c
// Version A
for (i=1; i<n; i++)
    for (j=1; j<n; j++)
        for (k=1; k<n; k++)
            c[i,j]+= a[i,k]*b[k,j]

// Version B
for (i=1; i<n; i++)
    for (k=1; k<n; k++)
        for (j=1; j<n; j++)
            c[i,j]+= a[i,k]*b[k,j]
```

A segunda versão do algoritmo é muito mais eficiente uma vez que usa a memória cache para proveito próprio: os cálculos são executados por linhas em vez de ser por colunas, resultando numa menor quantidade de *cache miss* e I/O envolvendo o disco.

