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