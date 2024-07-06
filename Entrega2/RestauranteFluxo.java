import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;

class Restaurante {

    //semáforo e lock
    private final Semaphore lugaresDisponiveis;
    private final Lock lock = new ReentrantLock();
    private final Condition todosSaindo = lock.newCondition();

    private int clientesSentados = 0;
    private boolean todosSaindoAgora = false;

    //construtor
    public Restaurante(int numLugares) {
        this.lugaresDisponiveis = new Semaphore(numLugares);
    }

    public void entrarRestaurante(int idCliente) {
        lock.lock();
        try {
            while (todosSaindoAgora) {
                todosSaindo.await();
            }
            //se não houver lugares disponíveis, ele espera
            if (!lugaresDisponiveis.tryAcquire()) {
                todosSaindoAgora = true;
                System.out.println("Cliente " + idCliente + " está esperando porque está cheio.");
                todosSaindo.await();
                todosSaindoAgora = false;
                todosSaindo.signalAll();
            
            } else {
                clientesSentados++;
                System.out.println("Cliente " + idCliente + " entrou e sentou.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        //por fim
        } finally {
            lock.unlock();
        }
    }

    public void sairRestaurante(int idCliente) {
        lock.lock();
        try {
            
            System.out.println("Cliente " + idCliente + " terminou e saiu.");
            clientesSentados--;
            //libera o lugar
            lugaresDisponiveis.release();
            if (clientesSentados == 0) {
                todosSaindo.signalAll();
            }
        //por fim
        } finally {
            lock.unlock();
        }
    }
}

class Cliente implements Runnable {
    private final int idCliente;
    private final Restaurante restaurante;

    //construtor
    public Cliente(int idCliente, Restaurante restaurante) {
        this.idCliente = idCliente;
        this.restaurante = restaurante;
    }

    //clientes entram e saem do restaurante após comer
    @Override
    public void run() {
        restaurante.entrarRestaurante(idCliente);
        try {
            Thread.sleep(5000); //simulando o tempo do cliente comendo no restaurante
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        restaurante.sairRestaurante(idCliente);
    }
}

public class RestauranteFluxo {
    public static void main(String[] args) {
        Restaurante restaurante = new Restaurante(5);
        for (int i = 1; i <= 100; i++) {
            new Thread(new Cliente(i, restaurante)).start();
            try {
                Thread.sleep(1000); // Simula a chegada dos clientes em tempos diferentes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
