# Lei de Amdahl

A performance da aplicação está limitada à sua parte sequencial, onde não dá para introduzir o paralelismo. Segundo a Lei de Amdahl:

```note
speedup = 1 / (p/P + s)
    - s: parte sequencial
    - p: parte paralela
    - P: número de processadores
    - 1 = p + s
```

Assim, para um limite muito grande de processadores, o *speedup* está limitado a 1/s, ou seja, à percentagem da componente sequencial do código programado.

