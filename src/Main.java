import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.ThreadLocalRandom;

class Biblioteca {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition[] conditions = new Condition[10];
    private final boolean[] livros = new boolean[10];

    public Biblioteca() {
        for (int i = 0; i < 10; i++) {
            conditions[i] = lock.newCondition();
            livros[i] = true; // true indica que o livro está disponível
        }
    }

    public void emprestarLivro(int usuario, int livro) throws InterruptedException {
        lock.lock();
        try {
            while (!livros[livro]) {
                System.out.println("Usuário " + usuario + " - Esperando livro " + (livro + 1) + " ficar disponível");
                conditions[livro].await();
            }
            livros[livro] = false;
            System.out.println("Usuário " + usuario + " - Emprestou livro " + (livro + 1));
        } finally {
            lock.unlock();
        }
    }

    public void devolverLivro(int usuario, int livro) {
        lock.lock();
        try {
            livros[livro] = true;
            System.out.println("Usuário " + usuario + " - Devolveu livro " + (livro + 1));
            conditions[livro].signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class InicializaUsuario extends Thread {
    private int id;
    private Biblioteca biblioteca;

    public InicializaUsuario(int id, Biblioteca biblioteca) {
        this.id = id;
        this.biblioteca = biblioteca;
    }

    public void executar() {
        try {
            while (true) {
                int livro = ThreadLocalRandom.current().nextInt(10);
                biblioteca.emprestarLivro(id, livro);
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000)); // tempo com o livro
                biblioteca.devolverLivro(id, livro);
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000)); // tempo de espera para novo empréstimo
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Biblioteca biblioteca = new Biblioteca();
        Thread[] usuarios = new Thread[3];

        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            InicializaUsuario inicializaUsuario = new InicializaUsuario(id, biblioteca);
            usuarios[i] = new Thread(new Runnable() {
                public void run() {
                    inicializaUsuario.executar();
                }
            });
            usuarios[i].start();
        }

        for (Thread usuario : usuarios) {
            try {
                usuario.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
