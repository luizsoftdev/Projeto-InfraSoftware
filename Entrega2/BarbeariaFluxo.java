import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;

class Barbearia {

    //semáforo e lock
    private final Semaphore cadeirasDisponiveis;
    private final Lock lockBarbeiro = new ReentrantLock();
    private final Condition barbeiroDormindo = lockBarbeiro.newCondition();

    private boolean barbeiroOcupado = false;

    //construtor
    public Barbearia(int numCadeiras) {
        this.cadeirasDisponiveis = new Semaphore(numCadeiras);
    }

    public void entrarBarbearia(int idCliente) {
        
        if (cadeirasDisponiveis.tryAcquire()) {
            System.out.println("Cliente " + idCliente + " chegou e sentou na cadeira de espera.");
            lockBarbeiro.lock();
            try {
                while (barbeiroOcupado) {
                    System.out.println("Cliente " + idCliente + " está esperando sentado.");
                    barbeiroDormindo.await();
                }
                //acorda o barbeiro
                barbeiroOcupado = true;
                cadeirasDisponiveis.release();
                System.out.println("Cliente " + idCliente + " acordou o barbeiro.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            //por fim
            } finally {
                lockBarbeiro.unlock();
            }
        } else {
            System.out.println("Cliente " + idCliente + " foi embora porque estava cheio.");
        }
    }

    public void cortarCabelo(int idCliente) {
        System.out.println("Cliente " + idCliente + " está cortando o cabelo.");
        try {
            Thread.sleep(5000); //tempo arbitrário entre ele começar a cortar o cabelo até terminar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Cliente " + idCliente + " terminou o corte.");
        lockBarbeiro.lock();
        try {
            barbeiroOcupado = false;
            barbeiroDormindo.signal();
        //por fim para garantir
        } finally {
            lockBarbeiro.unlock();
        }
    }
}

class Cliente implements Runnable {
    private final int idCliente;
    private final Barbearia barbearia;

    //construtor
    public Cliente(int idCliente, Barbearia barbearia) {
        this.idCliente = idCliente;
        this.barbearia = barbearia;
    }

    //clientes tentam entrar na barbearia
    @Override
    public void run() {
        barbearia.entrarBarbearia(idCliente);
    }
}

class Barbeiro implements Runnable {
    private final Barbearia barbearia;

    //construtor
    public Barbeiro(Barbearia barbearia) {
        this.barbearia = barbearia;
    }

    //barbeiro corta o cabelo dos clientes
    @Override
    public void run() {
        while (true) {
            for (int i = 1; i <= 100; i++) {
                barbearia.cortarCabelo(i);
            }
        }
    }
}

public class BarbeariaFluxo{

    public static void main(String[] args) {
        Barbearia barbearia = new Barbearia(5); //inicializa o semáforo com n cadeiras, nesse caso escolhi 5

        //cria e inicia a thread do babeiro
        Thread barbeiroThread = new Thread(new Barbeiro(barbearia)); 
        barbeiroThread.start();

        for (int i =1; i<=100; i++) {
            new Thread(new Cliente(i, barbearia)).start();
            try {
                Thread.sleep(1000); //simulando os clientes chegando na barbearia
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}

