package es.ujaen.ssccdd.curso2023_24;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Procesos.AgenciaViajes;
import es.ujaen.ssccdd.curso2023_24.Procesos.ClienteParticular;
import es.ujaen.ssccdd.curso2023_24.Procesos.GestionViaje;
import es.ujaen.ssccdd.curso2023_24.Utils.TareaFinalizacion;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    private static final int NUM_CLIENTES = Numero_Aleatorio.nextInt( MIN_CLIENTES, MAX_CLIENTES );
    private static List<Semaphore> Sem_Clientes_Particulares = new ArrayList<>(NUM_CLIENTES);
    private static List<Semaphore> Sem_Agencias_Viajes = new ArrayList<>(NUM_CLIENTES);

    private static void InicializacionSemaforos( List<Semaphore> Sem_Clientes_Particulares, List<Semaphore> Sem_Agencias_Viajes, int Num_Usuarios ) {
        for ( int i = 0; i < Num_Usuarios; ++i ) {
            Sem_Clientes_Particulares.add( i, new Semaphore(0) );
            Sem_Agencias_Viajes.add( i, new Semaphore(0) );
        }
    }

    private static void CreacionEjecucionGestionViaje( ExecutorService Ejecucion_Procesos, List<Future<?>> Lista_Tareas, int Num_Clientes ){
        GestionViaje Nueva_Gestion = new GestionViaje( Num_Clientes );
        Future<?> Tarea_Gestion = Ejecucion_Procesos.submit( Nueva_Gestion );
        Lista_Tareas.add( Tarea_Gestion );
    }

    private static void CreacionEjecucionClientesParticulares( ExecutorService Ejecucion_Procesos, List<Future<?>> Lista_Tareas, int Num_Clientes ) {
        for ( int i = 0; i < Num_Clientes; ++i ) {
            ClienteParticular Nuevo_Cliente = new ClienteParticular( i, Sem_Clientes_Particulares );
            Future<?> Nuevo_Proceso = Ejecucion_Procesos.submit( Nuevo_Cliente );
            Lista_Tareas.add( Nuevo_Proceso );
        }
    }

    private static void CreacionEjecucionAgenciasViajes( ExecutorService Ejecucion_Procesos, List<Future<?>> Lista_Tareas, int Num_Clientes ) {
        for ( int i = 0; i < Num_Clientes; ++i ) {
            AgenciaViajes Nueva_Agencia = new AgenciaViajes( i, Sem_Agencias_Viajes );
            Future<?> Nuevo_Proceso = Ejecucion_Procesos.submit( Nueva_Agencia );
            Lista_Tareas.add( Nuevo_Proceso );
        }
    }

    private static void EsperaFinalizacionPrograma( ExecutorService Ejecucion_Procesos, ScheduledExecutorService Ejecucion, Semaphore Fin_Ejecucion, List<Future<?>> Lista_Tareas ) throws InterruptedException {
        Ejecucion.schedule( new TareaFinalizacion( Lista_Tareas, Fin_Ejecucion ), TIEMPO_EJECUCION, TimeUnit.SECONDS );
        Fin_Ejecucion.acquire();
        Ejecucion_Procesos.shutdown();
        Ejecucion.shutdown();
        Ejecucion_Procesos.awaitTermination(1, TimeUnit.SECONDS );
        Ejecucion.awaitTermination(1, TimeUnit.SECONDS );
    }

    public static void main(String[] args) throws InterruptedException {
        // Declaración de variable
        ScheduledExecutorService Ejecucion;
        ExecutorService Ejecucion_Procesos;
        Semaphore Fin_Ejecucion;
        List<Future<?>> Lista_Tareas;

        // Inicialización de variables
        Ejecucion = Executors.newScheduledThreadPool(1);
        Ejecucion_Procesos = Executors.newFixedThreadPool(NUM_CLIENTES + NUM_CLIENTES + NUM_GESTION_VIAJES);
        Lista_Tareas = new ArrayList<>();
        Fin_Ejecucion = new Semaphore(0 );
        InicializacionSemaforos( Sem_Clientes_Particulares, Sem_Agencias_Viajes, NUM_CLIENTES );

        // Cuerpo de ejecución
        System.out.println( "Hilo(Principal) Comienza su ejecución " );
        CreacionEjecucionGestionViaje(Ejecucion_Procesos,Lista_Tareas, NUM_CLIENTES );
        CreacionEjecucionClientesParticulares( Ejecucion_Procesos, Lista_Tareas, NUM_CLIENTES );
        CreacionEjecucionAgenciasViajes( Ejecucion_Procesos, Lista_Tareas, NUM_CLIENTES );

        // Resultados ejecucion.
        EsperaFinalizacionPrograma( Ejecucion_Procesos, Ejecucion, Fin_Ejecucion, Lista_Tareas );

        // Finalización
        System.out.println( " Hilo(Principal) Ha finalizado " );
    }
}