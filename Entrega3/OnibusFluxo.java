import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Parada {
    private final Lock lock = new ReentrantLock();
    private final Condition onibusChegou = lock.newCondition();
    private final List<Passageiro> passageiros = new ArrayList<>();
    private static final int capacidadeOnibus = 50;

    public void passageiroChegaNaParada(Passageiro passageiro) {
        lock.lock();
        try {
            passageiros.add(passageiro);
            System.out.println("Passageiro " + passageiro.getId() + " está no aguardo.");
            onibusChegou.await(); // Passageiro espera até que o ônibus chegue
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void onibusChegaNaParada() {
        lock.lock();
        try {
            System.out.println("Onibus chegou");
            onibusChegou.signalAll(); // Sinaliza que o ônibus chegou

            // Retorna o tamanho da lista de passageiros atual ou o tamanho da capacidade do ônibus
            int passageirosEmbarcando = Math.min(passageiros.size(), capacidadeOnibus);
            System.out.println("Onibus embarcando " + passageirosEmbarcando + " passageiros.");

            List<Passageiro> embarcados = new ArrayList<>();
            for (Passageiro passageiro : passageiros.subList(0, passageirosEmbarcando)) {
                System.out.println("Passageiro " + passageiro.getId() + " entrou no ônibus.");
                embarcados.add(passageiro);
            }
            //remoção dos passageiros que embarcaram da lista de passageiros
            passageiros.removeAll(embarcados);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}

class Passageiro extends Thread {
    private final Parada parada;
    private final int id;

    public Passageiro(Parada parada, int id) {
        this.parada = parada;
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public void run() {
        parada.passageiroChegaNaParada(this);
    }
}

class Onibus extends Thread {
    private final Parada parada;

    public Onibus(Parada parada) {
        this.parada = parada;
    }

    @Override
    public void run() {
        try {
            while (true) {
                parada.onibusChegaNaParada();
                Thread.sleep((int) (1000 + Math.random() * 2000)); // varia de 1 até 3 segundos
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class OnibusFluxo {
    public static void main(String[] args) {
        Parada parada = new Parada();
        Onibus onibus = new Onibus(parada);
        onibus.start();

        for (int i = 1; i <= 200; i++) {
            Passageiro passageiro = new Passageiro(parada, i);
            passageiro.start();
            try {
                Thread.sleep(50); //50 milisegundos de intervalo entre a chegada de cada passageiro
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}