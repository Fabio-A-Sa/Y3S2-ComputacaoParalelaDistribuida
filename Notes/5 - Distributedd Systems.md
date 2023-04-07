# Sistemas Distribuidos

Conjunto de processos separados espacialmente mas que comunicam entre si com redes dedicadas. O sistema é distribuído se o atraso na transmissão das mensagens não for despresável comparado com o tempo entre eventos de um processo único.

Uma **mensagem** é uma sequência de bits atómica e formal que segue um protocolo. A latência (intervalo de tempo entre a emissão e recepção de uma mensagem) atualmente é o que interfere mais com a velocidade da propagação dos sinais na rede, devido aos limites físicos. Antigamente era a largura de banda. Exemplos de sistemas distribuidos:

- Web e Internet;
- Email;
- FEUP File System;
- P2P;

Vantagens de sistemas distribuidos:

- Partilha de recursos;
- Aceder a recursos remotos;
- Performance: pode usar vários computadores para resolver um só problema;
- Escalabilidade: tanto a nível geográfico como administrativo;
- Tolerância a falhas: *reliability* (confiável) e *availability* (disponível);

## Comunication Channels

Os canais de comunicação entre processos / entidades de um sistema distribuído podem:
- ser *connection-based* ou *connectionless*, 
- ser confiáveis ou não, na medida de perderem ou não dados pelo caminho;
- assegurar ordem das mensagems ou não;
- ser *message-based* ou *stream-based*;
- com ou sem controlo de fluxo;