import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ContaBancaria {
    private double saldo;
    private final Lock lock = new ReentrantLock();
    private final Condition saldoSuficiente = lock.newCondition();

    public ContaBancaria(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void depositar(double quantia) {
        lock.lock();
        try {
            saldo += quantia;
            System.out.println(Thread.currentThread().getName() + " depositou: " + quantia + " - Saldo atual: " + saldo);
            saldoSuficiente.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void sacar(double quantia) {
        lock.lock();
        try {
            while (saldo < quantia) {
                System.out.println(Thread.currentThread().getName() + " aguardando para sacar: " + quantia + " - Saldo atual: " + saldo);
                saldoSuficiente.await();
            }
            saldo -= quantia;
            System.out.println(Thread.currentThread().getName() + " sacou: " + quantia + " - Saldo atual: " + saldo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public double getSaldo() {
        lock.lock();
        try {
            return saldo;
        } finally {
            lock.unlock();
        }
    }
}

class Cliente extends Thread {
    private final ContaBancaria conta;
    private final boolean depositar;
    private final double quantia;

    public Cliente(ContaBancaria conta, boolean depositar, double quantia) {
        this.conta = conta;
        this.depositar = depositar;
        this.quantia = quantia;
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) { 
            if (depositar) {
                conta.depositar(quantia);
            } else {
                conta.sacar(quantia);
            }
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class SistemaBancario {
    public static void main(String[] args) {
        ContaBancaria conta = new ContaBancaria(100.0);

        Thread cliente1 = new Cliente(conta, true, 50.0);
        Thread cliente2 = new Cliente(conta, false, 150.0);
        Thread cliente3 = new Cliente(conta, true, 200.0);
        Thread cliente4 = new Cliente(conta, false, 100.0);
        Thread cliente5 = new Cliente(conta, false, 50.0);

        cliente1.setName("Cliente1");
        cliente2.setName("Cliente2");
        cliente3.setName("Cliente3");
        cliente4.setName("Cliente4");
        cliente5.setName("Cliente5");

        cliente1.start();
        cliente2.start();
        cliente3.start();
        cliente4.start();
        cliente5.start();

        try {
            cliente1.join();
            cliente2.join();
            cliente3.join();
            cliente4.join();
            cliente5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Saldo final: " + conta.getSaldo());
    }
}



