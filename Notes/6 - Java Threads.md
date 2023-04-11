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

Os threads podem ser interrompidos:

```java
HelloRunnable2 r1 = new HelloRunnable2(1);
Thread t1 = new Thread(r);
System.out.println("Before");
t1.start();
Thread.sleep(100); 
t1.interrupt();
```

