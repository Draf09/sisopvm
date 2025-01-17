// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// Fase 1 - máquina virtual (vide enunciado correspondente)
//


import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;


public class Sistema {

    public InterruptHandler interruptHandler;
    public static Programas progs;
    public GerenciadorMemoria gm;
    public GerenciadorProcessos gp;
    private Escalonador escalonador;
    public int[] registradores;
    public Word instructionRegister;
    public Interrupts interrupt;
    private LinkedList<PCB> prontos;
    private Word[] memory;
    private CPU cpu;
    private Console console;

    int tamMemoria;
    int tamPagina;
    int maxInt;
    int deltaMax;

    private LinkedList<PCB> bloqueados;
    private Semaphore escSemaforo = new Semaphore(0);
    private Semaphore cpuSemaforo = new Semaphore(0);

    private ConcurrentLinkedQueue<ChamadaConsole> pedidos = new ConcurrentLinkedQueue<>();
    //private Console console;

//    public Sistema(Semaphore appSemaforo) {
//        instructionRegister = new Word(Opcode.___,-1,-1,-1);
//        // cria a memória
//        memory = new Word[tamMemoria];
//        for (int i = 0; i < tamMemoria; i++) {
//            memory[i] = new Word(Opcode.___, -1, -1, -1);
//        }
//        prontos = new LinkedList();
//        bloqueados = new LinkedList();
//        pedidos = new ConcurrentLinkedQueue();
//        gm = new GerenciadorMemoria(gm.mem, tamPagina);
//        escalonador = new Escalonador();
//        gp = new GerenciadorProcessos(gm, gm.mem, cpu, escSemaforo, escalonador);
//        interruptHandler = new InterruptHandler();
//        cpu = new CPU(memory, tamPagina, maxInt, deltaMax, registradores, interrupt, instructionRegister, interruptHandler);
//        console = new Console(pedidos, memory, cpu, escSemaforo );
//        //this.executa();
//    }
    public Sistema(int tamMemoria, int tamanhoPagina, int maxInt, int quantidadeRegistradores, int deltaMax){   // a VM com tratamento de interrupções
        registradores = new int[quantidadeRegistradores];
        interrupt = Interrupts.INT_NONE;
        instructionRegister = new Word(Opcode.___,-1,-1,-1);
        // cria a memória
        memory = new Word[tamMemoria];
        for (int i = 0; i < tamMemoria; i++) {
            memory[i] = new Word(Opcode.___, -1, -1, -1);
        }
        // cpu
        cpu = new CPU(memory, tamanhoPagina, maxInt, deltaMax, registradores, interrupt, instructionRegister, interruptHandler);
        gm = new GerenciadorMemoria(memory, tamanhoPagina);
        gp = new GerenciadorProcessos(gm, gm.mem, cpu, escSemaforo, escalonador);
        progs = new Programas();
        bloqueados = new LinkedList();
        //Semaphore escSemaforo = new Semaphore(1);
        this.interruptHandler = new InterruptHandler();
        interruptHandler.setAttributes(gm,gp,escalonador, bloqueados);
        //GerenciadorMemoria gerMem, GerenciadorProcessos gerProc, Escalonador escalonador, LinkedList<PCB> bloqueados
        //rotinas.setAttributes(gp, escalonador, escSemaforo, memory, bloqueados, pedidos, appSemaforo);
        this.escalonador = new Escalonador();
        //console = new Console(pedidos, memory, cpu, escSemaforo );
        //gm = new GerenciadorMemoria( escSemaphore, cpuSemaphore, int, tamPagina);
    }


    // Fase 5 - Para demonstrar o funcionamento, você deve ter um sistema iterativo.
    // Uma vez que o sistema esteja funcionando, ele fica esperando comandos.
    // Os comandos possíveis são:
    // cria nomeDePrograma - cria um processo com memória alocada, PCB, etc. que fica em uma lista de processos.
    // esta chamada retorna um identificador único do processo no sistema (ex.: 1, 2, 3 …)
    // executa id - executa o processo com id fornecido. se não houver processo, retorna erro.
    // dump id - lista o conteúdo do PCB e o conteúdo de cada frame de memória do processo com id
    // dumpM inicio fim - lista os frames de memória entre início e fim, independente do processo
    // desaloca id - retira o processo id do sistema, tenha ele executado ou não

    public int cria(Word[] programa){
        int idDoProcessoCriado;
        idDoProcessoCriado = gp.criaProcesso(programa);
        if (idDoProcessoCriado==-1){
            System.out.println("Falta memoria para rodar o programa");
            return idDoProcessoCriado;
        }

        System.out.println("---------------------------------- programa carregado ");
        gm.dumpMemoriaUsada(gm.mem);
        return idDoProcessoCriado;
    }

    public void executa(int processId) {
        System.out.println("Iniciando escalonador");
        escalonador.setName("Escalonador");
        escalonador.run();

        System.out.println("Iniciando execução do processo");

        int [] paginasAlocadas = gp.getPaginasAlocadas(processId);
        if (paginasAlocadas[0]==-1){
            System.out.println("Processo não existe");
            return;
        }
        System.out.println("Páginas alocadas");
        for (int i=0; i<paginasAlocadas.length; i++){
            System.out.println(paginasAlocadas[i] + " ");
        }

        PCB running = gp.getProcesso(processId);

        // pega o contexto do processo que está rodando
        int programCounterDoRunning = running.getProgramCounter();
        int [] paginasAlocadasDoRunning = running.getPaginasAlocadas();
        int [] registradoresdoRunning = running.getRegistradores();
        Word instructionRegisterDoRunning = running.getInstructionRegister();
        Interrupts interruptsDoRunning = running.getInterrupt();

        cpu.setContext(programCounterDoRunning, paginasAlocadasDoRunning, registradoresdoRunning, instructionRegisterDoRunning, interruptsDoRunning);
        cpu.run();
        //console.run();
        System.out.println("---------------------------------- programa executado ");
        for (int i=0; i<paginasAlocadas.length; i++){
            gm.dumpPagina(gm.mem, paginasAlocadas[i]);
        }
    }

    public void listaProcessos(){
        gp.listaProcessos();
    }

    public void executaComEscalonador() { //esta funcao deve sair
        System.out.println(" ");
        System.out.println("Iniciando execução dos processos prontos com escalonador");
        cpu.setEscalonadorState(true);
        prontos = gp.getProntos();
        PCB running = prontos.getFirst();
        gp.setRunning(running);

        // pega o contexto do processo que está rodando
        int programCounterDoRunning = running.getProgramCounter();
        int [] paginasAlocadasDoRunning = running.getPaginasAlocadas();
        int [] registradoresdoRunning = running.getRegistradores();
        Word instructionRegisterDoRunning = running.getInstructionRegister();
        Interrupts interruptsDoRunning = running.getInterrupt();

        cpu.setContext(programCounterDoRunning, paginasAlocadasDoRunning, registradoresdoRunning, instructionRegisterDoRunning, interruptsDoRunning);

        cpu.run();
        System.out.println("---------------------------------- Escalonador executado ");
        //gm.dumpMemoriaUsada(vm.m);
    }

    public void dump (int processId){
        System.out.println("----------- dump do processo " + processId + "------------------");
        int [] paginasAlocadas = gp.getPaginasAlocadas(processId);

        for (int i=0; i<paginasAlocadas.length; i++){
            gm.dumpPagina(gm.mem, paginasAlocadas[i]);
        }
    }

    public void dumpM (int inicio, int fim){
        System.out.println("----------- dump com inicio em " + inicio + " e fim em " + fim);
        for (int i = inicio; i<=fim; i++){
            //System.out.println("fazendo dumop da página " + i);
            gm.dumpPagina(gm.mem, i);
        }
    }

    public void desaloca (int processId){
        PCB processo = gp.getProcesso(processId);
        int [] paginasAlocadas = gp.getPaginasAlocadas(processId);
        gp.desalocaProcesso(processo);
        System.out.println("--------------Processo " + processId + " desalocado---------------");
        for (int i=0; i<paginasAlocadas.length; i++){
            gm.dumpPagina(gm.mem, paginasAlocadas[i]);
        }
    }
}