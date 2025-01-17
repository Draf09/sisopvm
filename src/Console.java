import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Console extends Thread {

    private ConcurrentLinkedQueue<ChamadaConsole> pedidos;
    private Word[] memory;
    private CPU cpu;
    private Scanner in;
    public static Semaphore SEMA_CONSOLE = new Semaphore(0);
    private Semaphore appSemaforo;
    private int appEntrada;

    public Console(ConcurrentLinkedQueue<ChamadaConsole> pedidos, Word[] memory, CPU cpu, Semaphore appSemaforo) {
        in = new Scanner(System.in);
        this.pedidos = pedidos;
        this.memory = memory;
        this.cpu = cpu;
        this.appSemaforo = appSemaforo;
    }

    public void setEntrada(int entrada ){
        appEntrada = entrada;
        appSemaforo.release();
    }

    public int translateMemory(int[] allocatedPages, int address) {
        return (allocatedPages[(address / 16)] * 16) + (address % 16);
    }

    public void run() {
        while (true) {
            try {
                SEMA_CONSOLE.acquire();

                if (pedidos.isEmpty()) {
                    continue;
                }

                ChamadaConsole chamadaConsole = pedidos.poll();
                String type = chamadaConsole.getType();

                if (type.equals("IN")) {

                    System.out.println("Esperando entrada do usuário no console. Ex: \"c 3\"");
                    try {
                        appSemaforo.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int position = translateMemory(chamadaConsole.getAllocatedPages(), chamadaConsole.getMemoryAddress());
                    memory[position] = new Word(Opcode.DATA, -1, -1, appEntrada);
                    cpu.callIOInterrupt();
                    appEntrada = -2;
                } else if (type.equals("OUT")) {
                    int position = translateMemory(chamadaConsole.getAllocatedPages(), chamadaConsole.getMemoryAddress());
                    System.out.println("\nSaída do console: " + memory[position] + "\n");
                    cpu.callIOInterrupt();
                }
            } catch (InterruptedException e) {}
        }
    }
    public static void wait(int _milliseconds){    // Pausa o terminal por um tempo determinado.tempo de espera em milissegundos.
        try{
            Thread.sleep(_milliseconds);
        } catch(Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}
