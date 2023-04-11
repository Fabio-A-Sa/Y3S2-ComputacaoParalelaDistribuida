# Sistemas Distribuidos

Conjunto de processos separados espacialmente mas que comunicam entre si com redes dedicadas. O sistema é distribuído se o atraso na transmissão das mensagens não for despresável comparado com o tempo entre eventos de um processo único.

Uma **mensagem** é uma sequência de bits atómica e formal que segue um protocolo. A latência (intervalo de tempo entre a emissão e recepção de uma mensagem) atualmente é o que interfere mais com a velocidade da propagação dos sinais na rede, devido aos limites físicos. Antigamente era a largura de banda. Exemplos de sistemas distribuidos:

- Web e Internet;
- Email;
- FEUP File System;
- P2P;

Vantagens de sistemas distribuídos:

- Partilha de recursos;
- Aceder a recursos remotos;
- Performance: pode usar vários computadores para resolver um só problema;
- Escalabilidade: tanto a nível geográfico como administrativo;
- Tolerância a falhas: *reliability* (confiável) e *availability* (disponível);

## Comunication Channels

Os canais de comunicação entre processos / entidades de um sistema distribuído podem:
- ser *connection-based* ou *connectionless*, mediante seja necessário criar canais de comunicação antes da troca de dados;
- ser confiáveis ou não, na medida de perderem ou não dados pelo caminho ou haver duplicação de pacotes;
- assegurar ordem das mensagems ou não. Manter a ordem é importante para ver se não houve perdas no caminho;
- ser *message-based* ou *stream-based*;
- ter ou não controlo de fluxo;

## Internet Protocols

### UDP

Com base em mensagens (*datagrams*), é *connectionless* e não tem garantias da recepção da mensagem, podendo haver perdas e duplicados do lado do receptor. Não tem controlo de fluxo e permite multicast: invocando uma única vez `send()` é possível enviar uma cópia das mensagens entre vários processos. <br>
É mais simples de implementar do que TCP, logo é preferível para um sistema sem garantias: por exemplo carregamento de vídeos do youtube, música, rádio.

### TCP

São canais `stream` bidirecionais, ou seja, é possível enviar dados ao mesmo tempo em sentidos opostos. É orientada à conexão e é confiável: garante ordem das mensagens, não repetição das mesmas e controlo de fluxo. Como um canal TCP é identificado pelo par (IP Address, TCP Port), então terá de haver um `send()` por cada processo em multicast, ao contrário do UDP. <br>
O custo destes mecanismos é grande, pelo que é só usado quando necessitamos mesmo.

## Streaming Applications

Aplicações multimédia cujo conteúdo é apresentado antes de ser completamente recebido, porque é implementado através de um buffer. São exemplos o Youtube, internet, rádio, Zoom. Não tolera grandes variações de largura de banda, nem uma elevada perda ou delay de recepção de pacotes. A compressão de conteúdo é importante para estas aplicações. 

## Remote Procedure Call (RPC)

Um paradigma mais familiar do que as mensagens enviadas por send() e receive(), porque assim já não depende do protocolo de comunicação usado e concede transparência ao sistema. É usado mais para aplicações client-server. É tipicamente implementada em cima da camada de transporte da aplicação.

O cliente invoca uma `client stub` e o servidor uma `server stub`, ambas funções locais. Estas sim comunicam com um protocolo entre si, usando uma chamada *remote procedure call*. Esta estratégia também é usada para System Calls (*trap functions*).

Como os sistemas de cliente e servidor podem ser diferentes (diferente linguagem, diferentes estruturas de dados e protocolos associados), é importante uniformizar a comunicação. Há duas formas:
- `Standardize format`: a comunicação é efetuada da forma X durante a transferência. Requer duas traduções das mensagens e pode não ser a forma mais eficiente;
- `Receiver-makes-right`: só do lado do servidor é que há tradução, se necessário. Isto implica que a própria mensagem contenha um identificador do tipo de plataforma usada;

### Falhas

O sistema RPC em caso de falhas não consegue dar o motivo da falha ao cliente: é impossível detectar se ocorreu um erro devido à request ou perda de dados no processo. 

Pode haver retransmissão de tramas de informação até receber uma ACK por parte do servidor. No caso de perdas de mensagens:

- `At-least-once`: se os pedidos ao servidor forem indepotentes (se a mesma chamada ao procedimento remoto produzir os mesmos resultados);
- `At-most-once`: se os pedidos ao servidor não forem indepotentes.

A abordagem `at-least-once` pode ser usada desde que do lado do cliente ou do servidor haja uma memória do estado da máquina/request anterior.

## Multicast Communication

Sempre que existe um emissor e vários receptores. Quando o número de receptores é igual ao número de máquinas no sistema distribuído chama-se **broadcast**. Para X senders e Y receivers é necessário haver X\*Y canais de comunicação (*point-to-point* networks).

Usando uma ST (*spanning tree*) é possível comunicar de forma eficiente, pois a árvore inclui todos os nós da rede, sem repetir arestas. Esta árvore é característica de cada *sender*.

As comunicações podem usar o IP através de canais UDP, mesmo que não sejam confiáveis. Só o IPv6 é que suporta multicast.

`Banana Tree Protocol` (BTP) é um protocolo de construção de redes mais eficiente do que qualquer MST ou SPT, que permite minimizar o custo das ligações ou o delay das mesmas:
- Se a árvore não estiver criada, o primeiro nó inserido é o root;
- Se a árvore estiver criada, então os nós seguintes terão como pai o root;
- Cada nó pode pedir para mudar de pai se for conveniente para a estratégia definida, através de um request que pode vir a ser rejeitado ou não.
