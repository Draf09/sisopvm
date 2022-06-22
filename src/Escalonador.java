import java.util.LinkedList;
import java.util.concurrent.Semaphore;
public class Escalonador extends Thread {
    public static Semaphore SEMA_ESC = new Semaphore(0);
    //private Semaphore SEMA_ESC;
    public int posicaoEscalonador;
    private CPU cpu;
    GerenciadorProcessos gp;
    PCB runningProcess;
    Word ir;

    public Escalonador() {
        super("Escalonador");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Espera processo pronto.
                System.out.println("antes da sc");
                SEMA_ESC.acquire(); // inicia com wait esperando bloqueados
                System.out.println("se isso apareceu, entrou na sc");
                if (gp.prontos.size() > 0) {
                    continue;
                }
                    System.out.println("pronto para os prontos...começando");
                    PCB pcb = gp.prontos.get(posicaoEscalonador);
                    this.runningProcess = pcb;
                    int old = posicaoEscalonador;
                    posicaoEscalonador = (posicaoEscalonador + 1) % gp.prontos.size();
                    gp.prontos.remove(old);
                    cpu.setContext(old, runningProcess.paginasAlocadas, runningProcess.registradores, ir , Interrupts.INT_SCHEDULER);
                    //gp.running.setContext();

                    PCB nextProcess = gp.running; //escalona , escolhe processo e restaura estado da CPU
                    System.out.println("\nEscalonando processo com ID = " + nextProcess.getId() + " ["
                            + gp.getProcesso(nextProcess.getId())+ "]\n"); // .getProgramNameByProcessId(nextProccess.getId()) + "]\n");
                    //loadPCB(nextProccess);
                    gp.nextProcess(nextProcess.id);

                    // CPU liberada.
                    //cpu.SEMA_CPU.release();
                    cpu.SEMA_CPU.release();
            } catch (InterruptedException error) {
                error.printStackTrace();
            }
        }
    }
}


//public class Escalonador extends Thread{
//    private LinkedList<PCB> prontos;
//    private int posicaoEscalonador;
//    int [] registradores;
//    int programCounter;
//    Word instructionRegister;
//    Interrupts interrupt;
//    public static Semaphore escSemaphore;
//    private Semaphore cpuSemaphore;
//    private CPU cpu;
//
//    private  GerenciadorProcessos gp;
//
//    public Escalonador(GerenciadorProcessos gp){
//        posicaoEscalonador = 0;
//        this.gp = gp;
//    }
//
//    @Override
//    //public void run(int programCounter, int [] registradores, Word instructionRegister, Interrupts interrupt, int[] paginasAlocadas){
//    public void run() {
//        while(true) {
//            try {
//                escSemaphore.acquire();
//                if (prontos.isEmpty()) continue;
//                PCB pcb = prontos.get(posicaoEscalonador);
//                this.gp.running = pcb;
//                // seta as variáveis do processo atual com estado atual da CPU
//                int old = posicaoEscalonador;
//                posicaoEscalonador =  (posicaoEscalonador + 1) % gp.prontos.size();
//                prontos.remove(old);
//                gp.running.setContext(programCounter, registradores, instructionRegister, interrupt);
//                gp.running = gp.prontos.get(posicaoEscalonador);
//                cpuSemaphore.release();
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    public void setAttributes(LinkedList<PCB> prontos, Semaphore escSemaphore, Semaphore cpuSemaphore, CPU cpu){
//        this.prontos = prontos;
//        this.posicaoEscalonador = 0;
//        this.escSemaphore = escSemaphore;
//        this.cpuSemaphore = cpuSemaphore;
//        this.cpu = cpu;
//    }
//
//    public PCB getRunninProcess() {
//        return gp.running;
//    }
//    public void setRunningProcessAsNull(){
//        this.gp.running = null;
//    }
//
//    public LinkedList<PCB> getProntos() {
//        return prontos;
//    }
//}
//
///**
// * Testas se a lista de processos esta vazia e depois rodar o escalonador (sem inicializado em 0) automaticamente
// *
// * depois de cria ele roda
// * */