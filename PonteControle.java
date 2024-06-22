import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Ponte {
    private final Lock lock = new ReentrantLock();
    private final Condition ponteVazia = lock.newCondition();
    private boolean ponteOcupada = false;

    public void entrarNaPonte(String direcao) throws InterruptedException {
        lock.lock();
        try {
            while (ponteOcupada) {
                ponteVazia.await();
            }
            ponteOcupada = true;
            System.out.println("Carro vindo da " + direcao + " está na ponte.");
        } finally {
            lock.unlock();
        }
    }

    public void sairDaPonte(String direcao) {
        lock.lock();
        try {
            ponteOcupada = false;
            System.out.println("Carro vindo da " + direcao + " saiu da ponte.");
            ponteVazia.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class CarroComControle implements Runnable {
    private final Ponte ponte;
    private final String direcao;

    public CarroComControle(Ponte ponte, String direcao) {
        this.ponte = ponte;
        this.direcao = direcao;
    }

    @Override
    public void run() {
        try {
            ponte.entrarNaPonte(direcao);
            Thread.sleep((long) (Math.random() * 1000)); 
            ponte.sairDaPonte(direcao);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class PonteControle {
    public static void main(String[] args) {
        Ponte ponte = new Ponte();
        for (int i = 0; i < 10; i++) {
            new Thread(new CarroComControle(ponte, "esquerda")).start();
            new Thread(new CarroComControle(ponte, "direita")).start();
        }
    }
}
