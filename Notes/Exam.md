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
UPD (connectionless, unreliable, unorder, datagrams, message based, without flow control, duplications) TCP (connection, reliable, order, flow control, stream based, without duplications). Message, sequencia de bits processados atomicamente, stream é uma pipe de sistemas Unix.
UDP necessita de especificar o endpoint (ipaddress, port) sempre que faz um send(), mas suporta multicast porque permite criar várias cópias e enviá-las a processos externos. EM TCP os canais podem ter a mesma port number.
Remote procedure call (standardize format or receiver-makes-right, que tem de ter uma flag para saber qual é o protocolo usado para o servidor poder traduzir e interpretar).
At least once (adequado para processos indepotentes) e At most once (para processos não indepotentes PNI). Para PNI os requests têm de ser acompanhados por um ID para fazer o "last remember" no server memory para garantias depois do reboot.
Para ser event-based precisa de ter operações I/O assícronas ou non-blocking. Para ser thread-based precisa de ter threads a nível de user-mode e de kernel-mode. As de kernel não sabem sa existência das de level, sendo responsabilidade da library fazer a interação entre estas. O user-based é mau porque a falha de uma página faz com que o único thread em kernel fique em wait e não pode ser usado para implementar e explorar paralelismo multicore. Os server architectures (paralell, I/O operations, Prog) são iteractive (n, blockiing, easy), multithread (yes, blocking, races), state machine (yes, non-blocking, event-driven).