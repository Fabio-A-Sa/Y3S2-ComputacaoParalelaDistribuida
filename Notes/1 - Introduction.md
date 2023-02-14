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

Estes dois indicadores não são suficientes para classificar a performance do sistema. É necessário que essa performance seja sustentada, ou seja, que dependa o menos possível de factores como a velocidade I/O, hierarquia de memória ou acessos à mesma. Muitas vezes a performance depende do desenho do algoritmo implementado:

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

## PAPI - Performance API

Derivado Yes -> a API infere quantas falhas houve. Não é retirado/contado diretamente do CPU. <TODO>

## Memórias Cache no impacto da Performance

Nas memórias, a dimensão é inversamente proporcional à rapidez. Por ordem decrescente de rapidez: registos, L1-cache, L2-cache, L<N>-cache, memória. Registos, L1 e L2 estão incorporadas no CPU, as restantes são externas. <br>
As operações que consomem mais tempo (mais ciclos de relógio) são aquelas que manipulam memória (paginação e segmentação de processos e dados associados). A manipulação de memória cache pode resultar em duas respostas:

- `Cache Hit`: quando os dados a manipular a seguir pelo CPU estão em memória cache. Não é necessário fazer swap;
- `Cache Miss`: quando os dados não estão em cache. É necessário fazer swap com o disco;

A utilização de memória cache garante dois tipos de localização:

- `Localização temporal`: se o bloco de dados foi usado recentemente, é muito provável que ainda esteja em cache;
- `Localização espacial`: se os dados necessários a seguir estiverem próximos no disco, é muito provável que estejam incluidos no mesmo bloco;

### Tipos de memória

`SRAM` - Static Random Access Memory, menor tempo de acesso e que mantém o valor ao longo do tempo;
`DRAM` - Dynamic Random Access Memory, maior tempo de acesso e não mantém o seu valor ao longo do tempo. Implementada com condensadores, por isso mais cara. Necessita de um circuito adicional para garantir o estado dos dados.

### Coerência de Cache

Cada núcleo do processador contém a sua própria cache e esta é requisitada antes de qualquer instrução. Para vários sistemas, uma atualização de um valor numa memória cache tem de inviabilizar ou atualizar os valores usados noutros processos que copiaram os mesmos dados. Uma possível implementação passa por permitir a leitura concorrente entre vários processos mas só um processo em modo de escrita (*Write-invalidate protocol*), sinalizando a sua modificação. Só quando os outros processos vão precisar desse valor e esse estiver sinalizado como inválido é que vão buscar à memória principal, reduzindo o impacto na performance.

A coerência de cache é só implementada em sistemas com relativamente **poucos núcleos** por motivos de eficiência. 

## Multiplicação de Matrizes por Bloco

