class Ponte {
    public void entrarNaPonte(String direcao) {
        System.out.println("Carro vindo da " + direcao + " está na ponte.");
    }

    public void sairDaPonte(String direcao) {
        System.out.println("Carro vindo da " + direcao + " saiu da ponte.");
    }
}

class CarroSemControle implements Runnable {
    private final Ponte ponte;
    private final String direcao;

    public CarroSemControle(Ponte ponte, String direcao) {
        this.ponte = ponte;
        this.direcao = direcao;
    }

    @Override
    public void run() {
        ponte.entrarNaPonte(direcao);
        try {
            Thread.sleep((long) (Math.random() * 1000)); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ponte.sairDaPonte(direcao);
    }
}

public class PonteSemControle {
    public static void main(String[] args) {
        Ponte ponte = new Ponte();
        for (int i = 0; i < 10; i++) {
            new Thread(new CarroSemControle(ponte, "esquerda")).start();
            new Thread(new CarroSemControle(ponte, "direita")).start();
        }
    }
}
