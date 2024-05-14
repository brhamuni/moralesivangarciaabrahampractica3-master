package es.ujaen.ssccdd.curso2023_24.Utils;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class TareaFinalizacion implements Runnable {
    private final List<Future<?>> Lista_Tareas;
    private final Semaphore Fin_Ejecucion;

    public TareaFinalizacion( List<Future<?>> Lista_Tareas, Semaphore Fin_Ejecucion ) {
        this.Lista_Tareas = Lista_Tareas;
        this.Fin_Ejecucion = Fin_Ejecucion;
    }

    @Override
    public void run() {
        System.out.println( "Ha iniciado la ejecución la Tarea(FINALIZACION)" );

        // Recorre la lista de tareas para solicitar su finalización
        for ( Future<?> tarea : Lista_Tareas ) {
            tarea.cancel(true );
        }

        System.out.println( "Ha finalizado la ejecución la Tarea(FINALIZACION)" );
        // El programa principal puede presentar los resultados de las tareas
        Fin_Ejecucion.release();
    }
}