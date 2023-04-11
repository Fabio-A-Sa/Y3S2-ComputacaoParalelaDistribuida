# Java Threads and Concurrency Control

## Interleaving

As threads em Java são não-determinísticas em relação à ordem de execução das instruções:

- Before, 1, 2, After, End
- Before, 2, 1, After, End
- Before, After, 1, 2, End
- Before, After, 2, 1, End
- Before, 1, After, 2, End
- Before, 2, After, 1, End

```java
public class HelloRunnable2 implements Runnable {

    int num ;
    public void run () { 
        System.out.println(num); 
    }
    
    HelloRunnable2 (int arg) { 
        num = arg; 
    }

    public static void main (String args []) {
        HelloRunnable2 r1 = new HelloRunnable2(1);
        HelloRunnable2 r2 = new HelloRunnable2(2);
        Thread t1 = new Thread(r1); 
        Thread t2 = new Thread(r2);
        System.out.println(" Before ");
        t1.start(); 
        t2.start();
        System.out.println(" After ");
        try { 
            t2.join();
            t1.join(); 
        } catch (InterruptedException e) {}

        System.out.println(" End ");
    }
}
```

Forçar a sincronização e ordem de execução de threads com `sleep` (Thread.sleep(miliseconds)) não é seguro: apesar de originar uma percentagem muito grande de acertos, esta nunca é 100%.


## Interrupt

Os threads podem ser interrompidos:

```java
HelloRunnable2 r1 = new HelloRunnable2(1);
Thread t1 = new Thread(r);
System.out.println("Before");
t1.start();
Thread.sleep(100); 
t1.interrupt();
```

## Executors

É só criado um thread único e este fica encarregue de executar várias tarefas (classes com funções run()):

```java
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorExample {

    public static void main(String[] args) {

        Executor executor = Executor.newSingleThreadExecutor();
        //Executor executor = Executor.newFixedThreadPoll(2);

        Runnable runnable1 = new Runnable() {
            public void run() {
                System.out.println("Hello from a Runnable task 1!");
            }
        };

        Runnable runnable2 = new Runnable() {
            public void run() {
                System.out.println("Hello from a Runnable task 2!");
            }
        };

        executor.execute(runnable1);
        executor.execute(runnable2);
        executor.shutdown();
    }
}
```

É implementado com base em filas de espera, onde podemos indicar quantas threads são responsáveis por realizar o trabalho da fila (*pool*). Por default é só uma:

```java
Executor executor = Executor.newFixedThreadPoll(2)
```

## Sincronização