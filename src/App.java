import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class App {

    public static void main(String[] args) throws InterruptedException {

        // Variáveis
        int tamanhoDamemoria = 1024;
        int tamanhoDaPaginadeMemoria = 16;
        int maxInt = 100_000;
        int quantidadeRegistradores = 10;
        int deltaMax = 5; // usado pelo escalonador para interromper a CPU a cada x ciclos

        Sistema s = new Sistema(tamanhoDamemoria, tamanhoDaPaginadeMemoria, maxInt, quantidadeRegistradores, deltaMax);
        Semaphore semaphore = new Semaphore(0);
        //Sistema s = new Sistema(semaphore);


        BootAnimation ba = new BootAnimation();
        ba.load();

        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.println("");
            System.out.println("---------------------------------------------");

            System.out.println("\nDigite um comando. Comandos disponíveis e exemplos de uso:\n\n" +
                    "- cria nomeDePrograma - exemplo: cria fibo\n" +
                    "- nomes de programas disponíveis: fibo, fato, bub\n" +
                    "- executa id          - exemplo: exe 2\n" +
                    "- dump id             - exemplo: dump 1\n" +
                    "- dumpM inicio fim    - exemplo: dumpM 2,5\n" +
                    "- desaloca id         - exemplo: des 1\n" +
                    "- listaProcessos      - exemplo: lista\n" +
                    "- executa escalonador - exemplo: esca\n" +
                    "- end                 - exemplo: end");

            String palavra = in.nextLine();

            if (palavra.equals("lista")){
                s.listaProcessos();
                Console.wait(800);
            }

            //s.executaComEscalonador();
            else if (palavra.equals("esca")){
                s.executaComEscalonador();
                Console.wait(800);
            }

            else if (palavra.equals("end")){
                System.out.println("Goodbye!");
                break;
            }

            else if (palavra.contains(" ")){
                String [] input = palavra.split(" ");
                String comando = input[0];
                String arg = input[1];

                if (comando.equals("cria")){
                    if (arg.equals("fibo")){
                        s.cria(Sistema.progs.fibonacci10);
                    }
                    else if (arg.equals("fato")){
                        s.cria(Sistema.progs.fatorial);
                    }
                    else if (arg.equals("bub")){
                        s.cria(Sistema.progs.bubbleSort);
                    }
                    else{
                        System.out.println("Programa desconhecido");
                    }
                }

                //s.executa(2);
                else if (comando.equals("exe")){
                    int processo = Integer.parseInt(arg);
                    s.executa(processo);
                    Console.wait(800);
                }

                //s.dump(2);
                else if (comando.equals("dump")){
                    int processo = Integer.parseInt(arg);
                    s.dump(processo);
                    Console.wait(800);
                }

                //s.desaloca(2);
                else if (comando.equals("des")){
                    int processo = Integer.parseInt(arg);
                    s.desaloca(processo);
                    Console.wait(800);
                }

                //s.dumpM(2,5);
                else if (comando.equals("dumpM")){
                    String [] numeros = arg.split(",");
                    int inicio = Integer.parseInt(numeros[0]);
                    int fim = Integer.parseInt(numeros[1]);
                    s.dumpM(inicio,fim);
                    Console.wait(800);
                }

                else{
                    System.out.println("Comando desconhecido!");
                }
            }

            else{
                System.out.println("Comando desconhecido!");
            }
        }
    }
}