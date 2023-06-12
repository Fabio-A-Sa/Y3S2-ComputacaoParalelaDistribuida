# Preparação para Exame

## Paralela

SRAM (static random access memory), menos tempo de acesso, mantém o valor. DynamicRAM implementada com condensadores, mais cara e ao contrário da outra.
O Write Invalidate Protocol é só implementado em sistemas com poucos núcleos
Em shared memory o sistema começa e acaba com 1 P, enquanto em Message Passing tem vários P logo desde o início.
False sharing quando dois processos acedem a zonas de memória próximas
private(var) cria uma cópia local para a memória do processo mas não dá assign (pode conter lixo), critical usa semáforos, atomic para pedaços de código pequenos, firstprivate(var) cria uma cópia da main, lastprivate(var) passa para fora o último valor computado, reduction(arg:var) é mais eficiente que uma critial section.
pragma omp master/single/barrier
schedule static (pouco overhead) dynamic (tem overhead e só compensa quando os T são desiquilibrados) guided (alocação heurística, mais no inicio do que no fim).
aumentar o chunk diminui o overhead mas não permite tão bom balanceamento. em schedule static C aloca chunks de size C (AABBCCAABBCC), shedule dynamic C aloca C iterações dinamicas (AABBAACCAABBAACC).
em tasks as variáveis são first private, para utilizar os seus valores fora tem de fazer shared(var).
uma região controlada por tasks é mais eficiente porque nenhuma T pode entrar e sair da região, cada T cria um número de filhos necessários, é apenas usada uma região paralela.
escalamento forte quando o problema é resolvido X vezes mais rápido, fraco quando um problema X vezes maior é resolvido no mesmo tempo, vertical quando se dá mais recursos à máquina, horizontal quando há mais máquinas.
a qualidade da paralelização é medida em speedup/processadoes. escalávem se a linha de isoeficiência de n em relação a P se mantém constante. speedup relativo quando o tempo é medido em qualquer nó, real quando é medido no nó mais eficiente.

## Distribuída

Distribuído quando os pontos de comunicação não são locais nem o delay é desprezável. 
UPD (connectionless, unreliable, unorder, datagrams, message based, without flow control, duplications) TCP (connection, reliable, order, flow control, stream based, without duplications)