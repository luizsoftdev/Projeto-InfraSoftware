import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;

class BanheiroUnissex {
    private final Lock lock = new ReentrantLock();
    private final Condition podeEntrar = lock.newCondition();
    private final Semaphore semaphore = new Semaphore(3);
    private String generoAtualDoBanheiro = null;
    private int pessoasNoBanheiro = 0;

    public void entrarNoBanheiro(Pessoa pessoa) throws InterruptedException {
        lock.lock();
        try {
            //verifica se o banheiro possui pessoas de gênero diferente ao da pessoa ou se o banheiro está cheio
            while ( (generoAtualDoBanheiro != null && !generoAtualDoBanheiro.equals(pessoa.getGender())) || semaphore.availablePermits() == 0) {
                podeEntrar.await();
            }

            semaphore.acquire(); //entrou no banheiro 

            
            if (pessoasNoBanheiro == 0) {
                //é a primeira pessoa a entrar no banheiro, logo o "gênero atual permitido" no banheiro é o gênero dessa pessoa
                generoAtualDoBanheiro = pessoa.getGender();
            }
            pessoasNoBanheiro++;

            System.out.println(pessoa.getGender()+ " " + pessoa.getId() +  " entrou no banheiro. Pessoas no banheiro: " + pessoasNoBanheiro);
        } finally {
            lock.unlock();
        }
    }

    public void sairDoBanheiro(Pessoa pessoa) {
        lock.lock();
        try {

            semaphore.release(); // saiu do banheiro
            pessoasNoBanheiro--;
            
            System.out.println(pessoa.getGender() + " " + pessoa.getId() + " saiu do banheiro. Pessoas no banheiro: " + pessoasNoBanheiro);
            if (pessoasNoBanheiro == 0) {
                generoAtualDoBanheiro = null; //banheiro está vazio
                podeEntrar.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}

class Pessoa extends Thread {
    private final BanheiroUnissex banheiro;
    private final String gender;
    private final long id;

    //Construtor
    public Pessoa(BanheiroUnissex banheiro, String gender, long id) {
        this.banheiro = banheiro;
        this.gender = gender;
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public long getId() {
        return this.id;
    }

    @Override
    public void run() {
        try {

            banheiro.entrarNoBanheiro(this);
            Thread.sleep((long) (Math.random() * 1000)); //Varia o tempo de cada pessoa no banheiro
            banheiro.sairDoBanheiro(this);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class BanheiroFluxo {
    public static void main(String[] args) {
        BanheiroUnissex banheiro = new BanheiroUnissex();

        for (int i = 0; i < 100; i++) { 

                String gender = Math.random() < 0.5 ? "Homem" : "Mulher"; //Proporção de homens e mulheres

                Pessoa pessoa = new Pessoa(banheiro,gender, i);   
                pessoa.start();

            try{
                Thread.sleep(50); //50 milisegundos de intervalo entre a chegada de cada pessoa
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }
}