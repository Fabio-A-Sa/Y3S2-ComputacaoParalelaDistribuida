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

Funções `synchronized` permite que threads diferentes fiquem bloqueados para leituras e escritas de variáveis, para não haver falta de coerência de valores:

```java
public class SynchronizedCounter {

    int counter = 0;

    public synchronized void increment() {
        counter++;
    }

    public synchronized void decrement() {
        counter--;
    }

    public synchronized int value() {
        return counter;
    }
}
```

Podemos também sincronizar apenas partes do código, partes de métodos, desde que sejam com objetos e não primitivas:

```java
public void addName(String name) {

    synchronized (this) {
        lastName = name;
        nameCount++;
    }
    nameList.add(name);
}
```

Quando queremos manipular com segurança variáveis de tipo primitivo:

```java
public class MyLunch {

    private long c1 = 0;
    private long c2 = 0;
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    public void inc1() {
        synchronized (lock1) {
            c1++;
        }
    }

    public void inc2() {
        synchronized (lock2) {
            c2++;
        }
    }
}
```

## Deadlocks

Evitar ciclos de `locks` pode ser realizado criando a sincronização sempre pela mesma ordem, independentemente do input:

```java
class Bank {

    private Account[] av;

    public boolean transfer(int from, int to, int amount) {

        Account low, hight;
        if (from < to) {
            low = av[from];
            high = av[to];
        } else {
            low = av[to];
            high = av[from];
        }

        synchronized (low) {
            synchronized (high) {
                av[from].withdraw(amout);
                av[to].deposite(amout);
            }
        }
    }
}
```