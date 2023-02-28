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

## Paralelismo Funcional

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

