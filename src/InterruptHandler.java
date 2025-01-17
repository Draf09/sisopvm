import java.util.LinkedList;
import java.util.Scanner;

public class InterruptHandler {
    private GerenciadorMemoria gerMem;
    private GerenciadorProcessos gerProc;
    private Escalonador escalonador;

    private LinkedList<PCB> bloqueados;

    public InterruptHandler(){
    }

    public void setAttributes(GerenciadorMemoria gerMem, GerenciadorProcessos gerProc, Escalonador escalonador, LinkedList<PCB> bloqueados){
        this.gerMem = gerMem;
        this.gerProc = gerProc;
        this.escalonador = escalonador;
        this.bloqueados = bloqueados;
    }

    public void dump(Word w) {
        System.out.print("[ ");
        System.out.print(w.opc);
        System.out.print(", ");
        System.out.print(w.r1);
        System.out.print(", ");
        System.out.print(w.r2);
        System.out.print(", ");
        System.out.print(w.p);
        System.out.println("  ] ");
    }

    public int traduzEndereco (int endereco){
        int [] paginasAlocadas = gerMem.getFramesAlocados();
        try {
            return (paginasAlocadas[(endereco / 16)] * 16) + (endereco % 16);
        } catch(ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    public void chamadaIO(Context context) {
        PCB processo = gerProc.getRunning();
        gerProc.setCPUforRunningProcess();
        processo.saveContext(context);


    }

    public boolean handleInterrupt(int[] registers, Word instructionRegister, Word[] memory, int programCounter, Interrupts interrupts) {
        switch (interrupts) {
            case INT_SCHEDULER:
                System.out.println("Escalonador acionado");
                //escalonador.runEscalonador(programCounter, registers, instructionRegister, interrupts, gerProc.running.getPaginasAlocadas());
                escalonador.run();
                escalonador.SEMA_ESC.release();
                return true;

            case INT_STOP:
                System.out.println("Ocorreu STOP no código, logo vamos remover o running");
                //gp.removeFromProntos(gp.running);  // se usar essa linha o programa permanece na memória e dá rpa ver o resultado
                gerProc.desalocaProcesso(gerProc.running); // assim o programa sai da memória, mas o dump é limpo

                // vê se ainda tem algum processo na lista e deixa esse como sendo o running
                if (gerProc.prontos.size()>0){
                    if (gerProc.posicaoEscalonador>0) gerProc.posicaoEscalonador = gerProc.posicaoEscalonador - 1;
                    gerProc.running = gerProc.prontos.get(gerProc.posicaoEscalonador);
                    gerProc.setCPUforRunningProcess();
                    escalonador.SEMA_ESC.release();
                    return true;
                }
                else{
                    return false;

                }

            case INT_INVALID_ADDRESS:
                System.out.println("Endereço inválido, na linha: " + programCounter);
                dump(memory[programCounter]);
                escalonador.SEMA_ESC.release();
                return false;

            // Consideramos, além de uma instrução inválida, o uso de um registrador inválido também
            case INT_NONE:
                System.out.println("Nenhuma interrupção" + interrupts);
                break;

            case INT_INVALID_INSTRUCTION:
                System.out.println("Comando desconhecido ou registrador inválido, na linha: " + programCounter);
                dump(memory[programCounter]);
                escalonador.SEMA_ESC.release();
                return false;

            case INT_OVERFLOW:
                programCounter--;
                System.out.println("Deu overflow, na linha: " + programCounter);
                dump(memory[programCounter]);
                escalonador.SEMA_ESC.release();
                return false;

            case INT_SYSTEM_CALL:
                // Entrada (in) (reg[8]=1): o programa lê um inteiro do teclado.
                // O parâmetro para IN, em reg[9], é o endereço de memória a armazenar a leitura
                // Saída (out) (reg[8]=2): o programa escreve um inteiro na tela.
                // O parâmetro para OUT, em reg[9], é o endereço de memória cujo valor deve-se escrever na tela

                Scanner in = new Scanner(System.in);

                if (registers[8] == 1) {
                    int address_destiny = traduzEndereco(registers[9]);
                    System.out.println("Insira um número:");
                    int value_to_be_written = in.nextInt();
                    memory[address_destiny].p = value_to_be_written;
                    escalonador.SEMA_ESC.release();
                    return true;
                }

                if (registers[8] == 2) {
                    int source_adress = traduzEndereco(registers[9]);
                    System.out.println("Output: " + memory[source_adress].p);
                    escalonador.SEMA_ESC.release();
                    return true;
                }
        }
        return true;
    }
}
