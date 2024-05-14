[![logo](https://www.gnu.org/graphics/gplv3-127x51.png)](https://choosealicense.com/licenses/gpl-3.0/)

# Tercera Práctica
## Resolución con paso de mensajes

Para la resolución de análisis y diseño se deberán utilizar paso de mensajes asíncronos como herramienta para la programación concurrente. También se utilizará la factoría [`Executors`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Executors.html) y la interface [`ExecutorService`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ExecutorService.html) para la ejecución de las tareas concurrentes que compondrán la solución de la práctica.

Para la implementación de la práctica se utilizará como herramienta de concurrencia JMS (Java Message Service). Esta práctica es una práctica en grupo de hasta dos alumnos y cada grupo deberá crear en el _broker_ sus propios _destinos_ para sus mensajes. Los miembros del grupo no tienen que pertenecer al mismo grupo de prácticas. Cada destino deberá definirse siguiendo la siguiente estructura:

```
....

// En la interface Constantes del proyecto

public static final String DESTINO = "ssccdd.curso2024.NOMBRE_GRUPO.BUZON";

...

```
 
El nombre del grupo tiene que ser único para los grupos, por lo que se recomienda usar alguna combinación de los nombres de los integrantes del grupo.

## Problema a resolver :  Servicio de Vacaciones

Hay que resolver un problema para un servicio de vacaciones que tendrá a su disposición rutas de vuelo o transporte terrestre a diferentes zonas. Además en esas zonas habrá hoteles donde los viajeros puedan alojarse en sus vacaciones. Las operaciones que se pueden realizar son las siguientes:

- Consulta de la disponibilidad
- Reserva para viajes y/o estancia
- Pago, con o sin cancelación
- Cancelación de reservas, si es posible hacerlo

Además hay dos tipos de procesos que podrán realizar las operaciones:

1. Usuarios particulares
2. Agencias de viajes que tendrán prioridad sobre usuarios particulares. Esta prioridad no puede ser estricta, es decir, los usuarios deben tener posibilidad para realizar sus operaciones.

Para su implementación en Java la herramienta será JMS en su modo asíncrono.

**NOTA:**
Como la práctica es en grupos de dos personas cada uno de los componentes del equipo deberá encargarse de uno de los dos tipos de procesos que realizan las operaciones. El desarrollo del servicio deberá hacerse de forma conjunta. En el repositorio deberá quedar constancia de las aportaciones de cada uno de los miembros del equipo. El desarrollo de la documentación deberá estar correctamente coordinada y no debe ser una suma de dos trabajos individuales.

## Solucion Tercera Práctica

### Constantes
Para la resolucion del proyecto definiremos las siguientes constantes.

* **MAX_CLIENTES:** Número máximo de clientes que van a realizar viajes.
* **MIN_CLIENTES:** Número mínimo de clientes que van a realizar viajes.
* **MAX_PLAZAS:** Número máximo de plazas que podrá tener un viaje.
* **MIN_PLAZAS:** Número mínimo de plazas que podrá tener un viaje.
* **PROBABILIDAD_CANCELACION:** Probabilidad de cancelacion de un viaje = 20.
* **TIEMPO_EJECUCION:** Tiempo establecido para que pasado ese tiempo se interrumpa la ejecución.


### Datos
Tipos de datos necesarios para la solución de la práctica:

* **TDA** TipoCliente
    *   Enumerado (`PARTICULAR`, `AGENCIA`)

* **TDA** TipoMensaje
    *   Enumerado (`CLIENTE `, `RESERVA`, `DISPONIBLE`, `CANCELACION`, `PAGAR`)

* **TDA** List< T >
    *   operaciones:
    *   `add(TipoElemento)` : inserta al final de la **Lista** un elemento.
    *   `remove()`: devuelve y elimina el primer elemento de la **Lista**.
    *   `empty()`: nos devuelve true si la **lista** está vacía

* **TDA** Cliente
    * Id : Integer
    * Nombre : String
    * Tipo : TipoCliente

* **TDA** Viaje
    * Id : Integer
    * Tipo : TipoViaje
    * Plazas_Disponibles : Integer
    * Precio : Float
    * Origen : String
    * Destino : String

* **TDA** Estancia
    * Id : Integer
    * Nombre : String
    * Plazas_Disponibles : Integer
    * Precio : Float


### Clase Mensaje
* **Variables**

        Tipo_Mensaje : TipoMensaje
        Nombre_Cliente : String
        Id_Cliente : Integer
        Tipo_Cliente : TipoCliente
        Num_Viaje : Integer
        Num_Estancia : Integer
        Cancelacion_Viaje : Boolean
        Reserva_Correcta : Boolean
        Pago_Correcto : Boolean
        Pago : Float
        Lista_Viajes_Disponibles : List<Viaje>
        Lista_Estancias_Disponibles : List<Estancia>

Diseño de los metodos usados en la clase Mensaje:

* **toString ( )**

        if( Tipo_Mensaje == "CLIENTE" ){
            return DatosCliente()
        }else if( Tipo_Mensaje == "RESERVA" ){
            return DatosReserva()
        }else if( Tipo_Mensaje == "DISPONIBILE" ){
            return DatosDisponibilidad()
        }else if( Tipo_Mensaje == "CANCELACION" ){
            return Cancelacion()
        }else if ( Tipo_Mensaje == "PAGAR" ){
            return Pagar()
        }

* **DatosCliente( )**

        return "Mensaje con los datos del cliente {" +
                "Nombre=" + Nombre_Cliente + ", ID=" + Id_Cliente +
                ", Tipo=" + Tipo_Cliente + ", Num_Viaje=" + Num_Viaje +
                ", Num_Estancia=" + Num_Estancia + ", Pago=" + Pago + '}'

* **DatosReserva( )**

         return "Mensaje con los datos de la reserva {"+
                "Nombre=" + Nombre_Cliente + ", ID" + Id_Cliente +
                ", Tipo=" + Tipo_Cliente + ", Num_Viaje=" + Num_Viaje +
                ", Num_Estancia=" + Num_Estancia + ", Cancelacion_Vaje=" + Cancelacion_Viaje + '}'

* **DatosDisponibilidad( )**

        return "Mensaje con los viajes y estancias disponibles {"+
                "Lista con los viajes=" + Lista_Viajes_Disponibles + 
                ", Lista con las estancias=" + Lista_Estancias_Disponibles + '}'

* **Cancelacion( )**

        return "Mensaje con los datos de la cancelacion {"+
                "Nombre=" + Nombre_Cliente + ", ID" + Id_Cliente +
                ", Tipo=" + Tipo_Cliente + ", Num_Viaje=" + Num_Viaje +
                ", Num_Estancia=" + Num_Estancia + ", Cancelacion_Vaje=" + Cancelacion_Viaje + '}'

* **Pagar( )**

        return "Mensaje con los datos de el pago de la reserva {"+
                "Nombre=" + Nombre_Cliente + ", ID" + Id_Cliente +
                ", Tipo=" + Tipo_Cliente + ", Num_Viaje=" + Num_Viaje +
                ", Num_Estancia=" + Num_Estancia + ", Cancelacion_Vaje=" + Cancelacion_Viaje + '}'


### Buzones
Los buzones que usaremos para recibir y enviar los mensajes en los distintos procesos seran los siguientes:

* **Preguntar_Disponibilidad :** Los mensajes de los clientes solicitando los viajes y estancias que hay disponibles actualmente se enviaran por este buzon.
* **Respuesta_Disponibilidad :** Los mensajes con los viajes y estancias que hay disponibles se enviaran por este buzon al cliente que lo ha solicitado.
* **Realizacion_Reserva :** Los mensajes con los datos del cliente para realizar la reserva del viaje y/o la estancia se enviaran por este buzón.
* **Confirmacion_Reserva :** Los mensajes indicando que la reserva se realizo correctamente se le enviaran al cliente por este buzon.
* **Realizacion_Pago :** Los mensajes con los datos del cliente una vez realizado el pago de la reserva se enviara por este buzon al proceso que gestiona los pagos.
* **Confirmacion_Pago :** Los mensajes indicando al cliente que el pago de la reserva se ha realizado correctamente se enviaran por este buzón.
* **Realizacion_Cancelacion :** Los mensajes dónde el cliente indica que quiere cancelar el viaje se enviaran por este buzon al proceso correspondiente.
* **Respuesta_Cancelacion :** Los mensajes donde se indica si la cancelacion se ha realizado correctamente se enviaran al cliente por este buzon.


### Diseño
Para simular las operaciones de los diferentes proceso se utilizan los siguientes métodos:

Métodos que simulan procesos en la clase Agencia Viaje y Cliente Particular:

* **ComprobarDisponibilidad( )** Este método simula como un cliente envia un mensaje a un gestor preguntando cuales son los viajes y estancias que hay disponibles para realizar.
* **RealizarReserva( Respuesta_Servidor : Mensaje )** Este método simula como los clientes le envian al gestor un mensaje con el viaje y la estancia que desean reservar.
* **RealizarPagoReserva( Respuesta_Servidor : Mensaje )** Este método simula como los clientes pagan la reserva y le envian el mensaje para que GestorViajes pueda comprobar que se realizo correctamente.
* **CancelarReserva( Respuesta_Servidor : Mensaje )** Este método simula como los clientes que tienen una reserva quieren cancelarla y por tanto envian un mensaje para que el gestor lleve acabo la cancelación.

Métodos que simulan procesos en la clase GestionViajes:

* **ComprobarSolicitudDisponibilidad( )** Este método simula como le llega al correo un mensaje de un cliente preguntando por la disponibilidad que hay y le responde con un mensaje con todo los viajes y estancias disponibles que hay.
* **ComprobarSolicitudReserva( )** Este método simula que le llega un mensaje de una reserva que ha de comprobarse para ver si esta disponible y una vez comprobada se enviara una respuesta al cliente indicando si se puede realizar la reserva o no.
* **ComprobarPagoReserva( )** Este método simula como le llega a la GestionViajes la factura del pago de la reserva y envia un mensaje al cliente como verificacion de que el pago se ha realizado correctamente.
* **ComprobarSolicitudCancelacion( )** Este método simula como recibe un mensaje de un cliente que ha realizado una reserva y desea cancelar el viaje, respondiendo al cliente que la cancelacion se ha realizado correctamente.


#### Proceso Cliente Particular

* **Variables**

        Nombre : String
        Id : Integer
        Num_Viaje : Integer
        Num_Estancia : Integer
        Respuesta_Servidor : Mensaje

        Destination Realizacion_Pago
        List<Destination> Confirmacion_Pago
        Destination Realizacion_Cancelacion
        List<Destination> Respuesta_Cancelacion
        Destination Realizacion_Reserva
        List<Destination> Confirmacion_Reserva
        Destination Preguntar_Disponibilidad
        List<Destination> Respuesta_Disponibilidad

* **Ejecución**

        ComprobarDisponibilidad()
        recive( Respuesta_Disponibilidad[Id], Respuesta_Servidor )

        RealizarReserva( Respuesta_Servidor )
        recive( Confirmacion_Reserva[Id], Respuesta_Servidor )

        if (!Respuesta.getReserva_Correcta()){
            escribir "La reserva no se puede realizar"
        }else{
            RealizarPagoReserva( Respuesta_Servidor )
            recive( Confirmacion_Pago[Id], Respuesta_Servidor )

            if (Respuesta.getCancelacion()){
                if (GenerarAleatorio(100) <= PROBABILIDAD_CANCELACION){  
                    CancelarReserva( Respuesta_Servidor )
                }else{
                    escribir "El cliente realiza el viaje"
                }
            }
        }

Diseño de los módulos presentes en el hilo de ejecución del Cliente

+ **ComprobarDisponibilidad()**

        Mensaje_Cliente : Mensaje 
        Mensaje_Cliente = generaMensaje(Datos_Cliente)
        send(Preguntar_Disponibilidad, Mensaje_Cliente)

+ **RealizarReserva(Respuesta_Servidor : Mensaje)**

        Elegir_Viaje, Elegir_Estancia : Bool
        Lista_Viajes_Disponibles : List<Viajes>
        Lista_Viajes_Disponibles : List<Estancia>
    
        Cancelacion = GenerarAleatorio(2)
        Elegir_Viaje = GenerarAleatorio(2)
        Elegir_Estancia = GenerarAleatorio(2)
    
        Lista_Viajes_Disponibles = Respuesta_Servidor.getLista_Viajes_Disponibles()
        Lista_Estancias_Disponibles = Respuesta_Servidor.getLista_Estancias_Disponibles()
    
        if( Elegir_Viaje == 0 Y Elegir_Estancia == 0){
            return
        }
        if( Elegir_Viaje == 1 Y Elegir_Estancia == 1){
            Viaje_Elegido = Lista_Viajes_Disponibles(GenerarAleatorio(Lista_Viajes_Disponibles.size()))
            Num_Viaje = Viaje_Elegido.getNum_Viaje()
      
            Estancia_Elegida = Lista_Estancias_Disponibles(GenerarAleatorio(Lista_Estancias_Disponibles.size()))
            Num_Estancia = Estancia_Elegida.getNum_Estancia()
        }else{
    
          if( Elegir_Viaje == 1 ){
              Viaje_Elegido = Lista_Viajes_Disponibles(GenerarAleatorio(Lista_Viajes_Disponibles.size()))
              Num_Viaje = Viaje_Elegido.getNum_Viaje()
          }
    
          if( Elegir_Estancia == 1 ){
              Estancia_Elegida = Lista_Estancias_Disponibles(GenerarAleatorio(Lista_Estancias_Disponibles.size()))
              Num_Estancia = Estancia_Elegida.getNum_Estancia()
          }
        }
          
        Respuesta_Servidor = generaMensaje( Datos_Reserva )
        send( Realizacion_Reserva, Respuesta_Servidor )


+ **RealizarPagoReserva(Respuesta_Servidor : Mensaje)**

        if( Respuesta_Servidor.getIdViaje()!=-1 ){
            Pago = Viaje_Elegido.getPrecio()
        }
        if( Respuesta_Servidor.getIdEstancia()!=-1 ){
            Pago = Pago + Estancia_Elegida.getPrecio()
        }
        if( Respuesta_Servidor.getCancelacion() ){
            // Incrementa hasta un 20%
            Pago = Pago*(1 + GenerarAleatorio(20)/100)
        }
        Peticion_Pago = generaMensaje(Respuesta_Servidor)
        send(Realizacion_Pago, Peticion_Pago)

+ **CancelarReserva(Respuesta_Servidor : Mensaje)**

        Peticion_Cancelacion = generaMensaje(Respuesta_Servidor)
        send(Realizacion_Cancelacion, Peticion_Cancelacion)


#### Proceso Agencia Viaje
* **Variables:**

      Nombre : String
      Id : Integer
      Num_Viaje : Integer
      Num_Estancia : Integer
      Respuesta_Servidor : Mensaje

      Destination Realizacion_Pago
      List<Destination> Confirmacion_Pago
      Destination Realizacion_Cancelacion
      List<Destination> Respuesta_Cancelacion
      Destination Realizacion_Reserva
      List<Destination> Confirmacion_Reserva
      Destination Preguntar_Disponibilidad
      List<Destination> Respuesta_Disponibilidad

* **Ejecución:**

      ComprobarDisponibilidad()
      recive( Respuesta_Disponibilidad[Id] , Respuesta_Servidor )

      RealizarReserva( Respuesta_Servidor )
      recive( Confirmacion_Reserva[Id] , Respuesta_Servidor )

      if ( !Respuesta_Servidor.getReserva_Correcta() ){
          escribir "La reserva no se ha podido llevar acabo."
      }else{
          RealizarPagoReserva( Respuesta_Servidor )
          recive( Confirmacion_Pago[Id] , Respuesta_Servidor )
          if ( Respuesta_Servidor.getCancelacion()){
              if (GenerarAleatorio(100) <= PROBABILIDAD_CANCELACION){  
                  CancelarReserva( Respuesta_Servidor );
                  recive( Respuesta_Cancelacion[Id] , Respuesta_Servidor )
              }else{
                  escribir "El cliente va a realizar el viaje."
              }
          }
      }

Diseño de los módulos presentes en el hilo de ejecución del Cliente

* **ComprobarDisponibilidad( )**

        Mensaje_Cliente : Mensaje 
        Mensaje_Cliente = GenerarMensaje( Datos_Cliente )
        send( Preguntar_Disponibilidad, Mensaje_Cliente )

* **RealizarReserva( Respuesta : Mensaje )**

        Elegir_Viaje, Elegir_Estancia : Boolean
        Lista_Viajes : List<Viaje>
        Lista_Estancias : List<Estancias>
    
        Cancelacion = GenerarAleatorio(2);
        Elegir_Viaje = GenerarAleatorio(2);
        Elegir_Estancia = GenerarAleatorio(2);
        Lista_Viajes = Respuesta.GetLista_Viajes_Disponibles()
        Lista_Estancias = Respuesta.GetLista_Estancias_Disponibles()
    
        if( Elegir_Viaje == 0 Y Elegir_Estancia == 0){
            return;    
        }
        if( Elegir_Viaje == 1 Y Elegir_Estancia == 1){
            Viaje_Elegido = Lista_Viajes.get( GenerarAleatorio(Lista_Viajes.size()) )
            Num_Viaje = Viaje_Elegido.getNum_Viaje()
            Estancia_Elegida = Lista_Estancias.get( GenerarAleatorio(Lista_Estancias.size()) )
            Num_Estancia = Estancia_Elegida.getNum_Estancia()
        }
        if( Elegir_Viaje == 1 Y Elegir_Estancia == 0){
            Viaje_Elegido =  Lista_Viajes.get( GenerarAleatorio(Lista_Viajes.size()) )
            Num_Viaje = Viaje_Elegido.getNum_Viaje()
        }
        if( Elegir_Viaje == 0 Y Elegir_Estancia == 1 ){
            Estancia_Elegida = Lista_Estancias.get( GenerarAleatorio(Lista_Estancias.size()) )
            Num_Estancia = Estancia_Elegida.getNum_Estancia()
        }
    
        Peticion_De_Reserva = GenerarMensaje( Datos_Reserva )
        send( Realizacion_Reserva, Peticion_De_Reserva )


* **RealizarPagoReserva( Respuesta_Servidor : Mensaje )**

        Pago : Float

        if( Respuesta_Servidor.getNum_Viaje() !=-1 ){
            Pago = Viaje_Elegido.getPrecio();
        }
        if( Respuesta_Servidor.getNum_Estancia() !=-1 ){
            Pago = Pago + Estancia_Elegida.getPrecio();
        }
        if( Respuesta.getCancelacion() ){
            Pago = Pago + GenerarAleatorio(50)
        }

        Realizacion_Pago = GenerarMensaje( Respuesta_Servidor, Pago )
        send( Realizacion_Pago, Realizacion_Pago )

* **CancelarReserva( Respuesta_Servidor : Mensaje )**

        Peticion_Cancelacion = GenerarMensaje( Respuesta_Servidor )
        send( Realizacion_Cancelacion, Peticion_Cancelacion )


#### Proceso Gestion Viajes

* **Variables:**

        Integer Num_Clientes
        List<Viaje> Lista_Viajes
        List<Estancia> Lista_Estancias

        List<Mensaje> Lista_Clientes_Particulares
        List<Mensaje> Lista_Agencias_Viaje
        List<Mensaje> Lista_Reservas
        List<Mensaje> Lista_Pagos
        List<Mensaje> Lista_Cancelaciones

        Destination Realizacion_Pago
        List<Destination> Confirmacion_Pago
        Destination Realizacion_Cancelacion
        List<Destination> Respuesta_Cancelacion
        Destination Realizacion_Reserva
        List<Destination> Confirmacion_Reserva
        Destination Preguntar_Disponibilidad
        List<Destination> Respuesta_Disponibilidad

* **Ejecucion:**


        CreacionInicializacionVariables()
        while( true ){
            recive( Preguntar_Disponibilidad , Peticion_Cliente )
            ComprobarSolicitudDisponibilidad()

            recive( Realizacion_Reserva , Peticion_Cliente )
            ComprobarSolicitudReserva()

            recive( Realizacion_Pago , Peticion_Cliente )
            ComprobarPagoReserva()

            recive( Realizacion_Cancelacion , Peticion_Cliente )
            ComprobarSolicitudCancelacion()
        }
        FinalizarEjecucion()

Diseño de los módulos presentes en el hilo de ejecución de GestionViajes:

* **ComprobarSolicitudDisponibilidad()**

        Receptor_Disponibilidad : Mensaje
        Tiempo_Esperado : Long
        
        if( !Lista_Agencias_Viaje.estaVacio() ){
            Datos_Disponibilidad : Mensaje

            ObtenerViajesDisponibles( Datos_Disponibilidad )
            ObtenerEstanciasDisponibles( Datos_Disponibilidad )

            if( !Lista_Clientes_Particulares.estaVacio(){
                Tiempo_Esperado = ObtenerTiempoEsperando( Lista_Clientes_Particulares.get(0) )

                if( Tiempo_Esperado > TIEMPO_MAXIMO_ESPERADO ){
                    Receptor_Disponibilidad = Lista_Clientes_Particulares.remove(0)
                }else{
                    Receptor_Disponibilidad = Lista_Agencias_Viaje.remove(0)
                }
            }else{
                Receptor_Disponibilidad = Lista_Agencias_Viaje.remove(0)
            }
            EnviarDisponibilidadCliente( Receptor_Disponibilidad, Datos_Disponibilidad )
        }else if( !Lista_Clientes_Particulares.estaVacio()){
           
            Datos_Disponibilidad : Mensaje
            ObtenerViajesDisponibles( Disponibilidad )
            ObtenerEstanciasDisponibles( Disponibilidad )
            
            Receptor_Disponibilidad = Lista_Clientes_Particulares.remove(0)
            EnviarDisponibilidadCliente( Receptor_Disponibilidad, Datos_Disponibilidad )
        }

* **ObtenerViajesDisponibles( Datos_Disponibilidad : Mensaje )**

        for( int i=0; i<Lista_Viajes.size(); ++i ){
            if( Lista_Viajes.get(i).GetNum_Plazas() > 0){
                Datos_Disponibilidad.GetListaViajesDisponibles.add( Lista_Viajes.get(i) )
            }
        }

* **ObtenerEstanciasDisponibles( Datos_Disponibilidad : Mensaje )**

        for( int i=0; i<Lista_Estancias.size(); ++i ){
            if( Lista_Estancias.get(i).GetNum_Plazas() > 0){
                Datos_Disponiblidad.GetListaEstanciasDisponibles.add( Lista_Estancias.get(i) )
            }
        }

* **EnviarDisponibilidadCliente( Receptor_Disponibilidad : Mensaje, Datos_Disponibilidad : Mensaje )**

        Mensaje_Enviar = GenerarMensaje ( Datos_Disponibilidad )
        send( Respuesta_Disponibilidad[Receptor_Disponibilidad.GetID()], Mensaje_Enviar ) 


* **ComprobarSolicitudReserva( )**

        if( !Lista_Reservas.estaVacio() ){
            Viaje_Disponible : Boolean
            Estancia_Disponible : Boolean
            Peticion_Reserva : Mensaje

            Peticion_Reserva = Lista_Reservas.remove(0)
            if( Peticion_Reserva.GetNum_Viaje() != -1 Y Peticion_Reserva.GetNum_Reserva == -1 ){
                Viaje_Disponible = ComprobarViajeReserva( Peticion_Reserva )
                if( Viaje_Disponible ){
                    Peticion_Reserva.SetReserva_Correcta( true )
                }else{
                    Peticion_Reserva.SetReserva_Correcta( false )
                }
                EnviarConfirmacionReservaCliente( Peticion_Reserva )
            }
            if( Peticion_Reserva.GetNum_Viaje() == -1 Y Peticion_Reserva.GetNum_Reserva != -1 ){
                Estancia_Disponible = ComprobarEstanciaReserva( Peticion_Reserva )
                if( Estancia_Disponible ){
                    Peticion_Reserva.SetReserva_Correcta( true )
                }else{
                    Peticion_Reserva.SetReserva_Correcta( false )
                }
                EnviarConfirmacionReservaCliente( Peticion_Reserva )
            }
            if( Peticion_Reserva.GetNum_Viaje() != -1 Y Peticion_Reserva.GetNum_Reserva != -1 ){
                Viaje_Disponible = ComprobarViajeReserva( Peticion_Reserva )
                Estancia_Disponible = ComprobarEstanciaReserva( Peticion_Reserva )
                if( Estancia_Disponible Y Viaje_Disponible ){
                    Peticion_Reserva.SetReserva_Correcta( true )
                }else{
                    Peticion_Reserva.SetReserva_Correcta( false )
                }
                EnviarConfirmacionReservaCliente( Peticion_Reserva )
            }
        }

* **Boolean : ComprobarViajeReserva( Peticion_Reserva : Mensaje)**

        for( int i=0; i<Lista_Viajes.size(); ++i ){
            if( Lista_Viajes.get(i).getNum_Viaje() == Peticion_Reserva.GetNum_Viaje() ){
                if( Lista_Viajes.get(i).GetNum_Plazas > 0 ){
                    return true
                }
            }
        }
        return false

* **Boolean : ComprobarEstanciaReserva( Peticion_Reserva : Mensaje )**

        for( int i=0; i<Lista_Viajes.size(); ++i ){
            if( Lista_Estancias.get(i).getNum_Estancia() == Peticion_Reserva.GetNum_Estancia() ){
                if( Lista_Estancias.get(i).GetNum_Plazas > 0 ){         
                    return true
                }
            }
        }
        return false

* **EnviarConfirmacionReservaCliente( Peticion_Reserva : Mensaje )**

        Mensaje_Enviar = GenerarMensaje ( Peticion_Reserva )
        send( Confirmacion_Reserva[Peticion_Reserva.GetID()], Mensaje_Enviar )


* **ComprobarPagoReserva()**

        if(!Lista_Pagos.vacia()){
            Pago_Reserva : Mensaje
            
            Pago_Reserva = Lista_Pagos.remove(0)
            Mensaje_Enviar = generarMensaje( Pago_Reserva )
            send ( Confirmacion_Pago ,Pago_Reserva )
        }


* **ComprobarSolicitudCancelacion( )**

        if( !Lista_Cancelacion.estaVacio() ){
            Peticion_Cancelacion : Mensaje

            Peticion_Cancelacion = Lista_Cancelacion.remove(0)
            EnviarCancelacionCliente( Peticion_Cancelacion )
        }

* **EnviarCancelacionCliente( Peticion_Cancelacion : Mensaje )**

        Mensaje_Enviar = GenerarMensaje ( Peticion_Cancelacion )
        send( Respuesta_Cancelacion[Peticion_Cancelacion.GetID()], Mensaje_Enviar )


#### Hilo Principal agh00040
* **Ejecucion:**

      CreacionGeneradores( Numero_Generadores_Aleatorio )
      CreacionEjecucionClientesParticulares( Numero_Particulares_Aleatorio )
      CreacionEjecucionGestionViajes()
      
      EsperarFinalizacion()
      FinalizaProcesos()
      FinalizaEjecucion()


#### Hilo Principal ims00051

        CreacionGeneradores( Numero_Generadores_Aleatorio )
        CreacionEjecucionAgencias_Viajes( Numero_Agencias_Aleatorio )

        EsperarFinalizacion()
        FinalizaProcesos()
        FinalizaEjecucion()

Cuando creamos los generadores el numero de generadores es aleatorio ya que depende del numero de Agencias de Viaje ya que cada vez que se ejecuta van a ser distintos debido a    que se generan de forma aleatoria.