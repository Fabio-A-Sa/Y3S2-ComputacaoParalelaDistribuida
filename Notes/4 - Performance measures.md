# Performance Measures

O escalamento de sistemas pode ser de dois tipos:
- Escalamento forte, quando o problema é resolvido X vezes mais rápido;
- Escalamento fraco, quando um problema X vezes maior é resolvido com o mesmo tempo de processamento;

O `speedup` é o rácio entre o tempo de processamento sequencial e processamento. Pode ser obtido usando três processos:
- Relativo, quando o tempo de execução sequencial é obtido executando o código paralelamente num único nó da máquina paralela;
- Real, quando é obtido através da execução do código mais eficiente num único nó da máquina paralela;
- Absoluto, quando é obtido através da execução do código mais eficiente num computador sequencial;

A eficiência é o rácio entre o speedup e o número de processadores.

Um sistema diz-se escalável se a eficiência permanecer constante mesmo aumentando o número de dados a calcular e o número de processadores.