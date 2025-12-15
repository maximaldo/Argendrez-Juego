package Principal.juego.red;

import java.io.IOException;
import Principal.juego.elementos.ColorPieza;
import Principal.juego.variantes.cartas.Ruleta;
import Principal.juego.variantes.cartas.TipoCarta;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Servidor UDP para el ajedrez.
 * Basado en HiloServidor del ejercicio de chat:
 *  - Escucha en el puerto 4321 por UDP
 *  - Responde a "Hello_there" con "General_Kenobi"
 *  - Maneja hasta 2 clientes
 *  - Reenvía los mensajes de uno al otro.
 *
 * Para el juego:
 *  - Al conectar el primer cliente le asigna COLOR:BLANCO
 *  - Al conectar el segundo, COLOR:NEGRO
 *  - Cuando ambos están conectados, envía "Conexion establecida" a los dos.
 *  - Los mensajes que empiezan con "MOVE:" se reenvían tal cual al otro jugador.
 */
public class ServidorAjedrez extends Thread {

    private DatagramSocket socket;

    private Cliente usuario1;
    private Cliente usuario2;

    private final Ruleta ruleta = new Ruleta();
    private ColorPieza turno = ColorPieza.BLANCO;

    private boolean partidaIniciada = false;
    private boolean fin = false;

    public ServidorAjedrez() {
        try {
            socket = new DatagramSocket(4321);
            System.out.println("[SERVER] Escuchando en puerto 4321");
        } catch (SocketException e) {
            System.err.println("[SERVER] ERROR: Puerto 4321 ocupado");
            fin = true;
        }
    }

    @Override
    public void run() {
        while (!fin) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket datagrama =
                    new DatagramPacket(buffer, buffer.length);

                socket.receive(datagrama);

                procesarMensaje(datagrama);

            } catch (IOException e) {
                if (!fin) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void procesarMensaje(DatagramPacket datagrama) {
        String mensaje = new String(
            datagrama.getData(),
            0,
            datagrama.getLength()
        ).trim();

        System.out.println("[SERVER] " + datagrama.getSocketAddress() + " -> " + mensaje);

        // Discovery por broadcast
        if (mensaje.equals("Hello_there")) {
            enviarMensaje("General_Kenobi", datagrama.getAddress(), datagrama.getPort());
            return;
        }

        // Petición de conexión de un cliente
        if (mensaje.equals("Conectar")) {
            conectarNuevoCliente(datagrama);
            return;
        }
        if (mensaje.equals("DISCONNECT")) {
            Cliente c = obtenerRemitente(datagrama);
            if (c != null) {
                desconectarCliente(c);
            }
            return;
        }


        //  NO aceptar acciones antes de que la partida esté iniciada
        if (!partidaIniciada) {
            System.out.println("[SERVER] Mensaje ignorado, partida no iniciada: " + mensaje);
            return;
        }


        Cliente emisor = obtenerRemitente(datagrama);

        if (emisor == null) {
            enviarMensaje(
                "No estas conectado",
                datagrama.getAddress(),
                datagrama.getPort()
            );
            return;
        }


        Cliente destino = obtenerOtro(emisor);

        // Para ajedrez nos interesa reenviar el mensaje TAL CUAL
        // (especialmente los "MOVE:x1,y1,x2,y2")
        enviarMensaje(mensaje, destino.ip, destino.puerto);
        turno = (turno == ColorPieza.BLANCO)
            ? ColorPieza.NEGRO
            : ColorPieza.BLANCO;

  // ===== RULETA (SOLO SERVIDOR) =====
        boolean daCarta = (turno == ColorPieza.BLANCO)
            ? ruleta.tickParaBlancas()
            : ruleta.tickParaNegras();

        int restante = (turno == ColorPieza.BLANCO)
            ? ruleta.getRestanteBlancas()
            : ruleta.getRestanteNegras();

   // enviar contador actualizado a ambos
        enviarMensaje(
            "RULETA:" + turno + "," + restante,
            usuario1.ip, usuario1.puerto
        );
        enviarMensaje(
            "RULETA:" + turno + "," + restante,
            usuario2.ip, usuario2.puerto
        );

        if (daCarta) {
            TipoCarta carta = ruleta.robarCarta();

            enviarMensaje(
                "DRAW:" + turno + "," + carta.name(),
                usuario1.ip, usuario1.puerto
            );
            enviarMensaje(
                "DRAW:" + turno + "," + carta.name(),
                usuario2.ip, usuario2.puerto
            );
        }
    }
    public void cerrarServidor() {
        fin = true;

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        interrupt();
    }

    private void desconectarCliente(Cliente c) {
        if (c == usuario1) usuario1 = null;
        if (c == usuario2) usuario2 = null;

        // Reset del estado de partida
        turno = ColorPieza.BLANCO;
        ruleta.reset(); // ahora vemos esto

        System.out.println("[SERVER] Cliente desconectado");
    }


    private void conectarNuevoCliente(DatagramPacket dp) {
        Cliente nuevo = new Cliente(dp);

        if (usuario1 == null) {
            usuario1 = nuevo;
            enviarMensaje("Conectado", nuevo.ip, nuevo.puerto);
            enviarMensaje("COLOR:BLANCO", nuevo.ip, nuevo.puerto);
            System.out.println("[SERVER] Primer jugador conectado (BLANCAS): " + nuevo);
            return;
        }

        if (usuario2 == null) {
            usuario2 = nuevo;
            enviarMensaje("COLOR:NEGRO", nuevo.ip, nuevo.puerto);

            enviarMensaje("Conexion establecida", usuario1.ip, usuario1.puerto);
            enviarMensaje("Conexion establecida", usuario2.ip, usuario2.puerto);

            partidaIniciada = true;
            return;
        }


        enviarMensaje(
            "Conexion denegada",
            nuevo.ip,
            nuevo.puerto
        );
    }

    private boolean conexionEstablecida() {
        return usuario1 != null && usuario2 != null;
    }

    private Cliente obtenerRemitente(DatagramPacket dp) {
        if (usuario1 != null && usuario1.esEste(dp)) return usuario1;
        if (usuario2 != null && usuario2.esEste(dp)) return usuario2;
        return null;
    }

    private Cliente obtenerOtro(Cliente emisor) {
        return emisor == usuario1 ? usuario2 : usuario1;
    }

    private void enviarMensaje(String mensaje, InetAddress ipDestino, int puertoDestino) {
        byte[] buffer = mensaje.getBytes();
        DatagramPacket datagrama =
            new DatagramPacket(buffer, buffer.length, ipDestino, puertoDestino);

        try {
            socket.send(datagrama);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ===================

    private static class Cliente {
        InetAddress ip;
        int puerto;

        Cliente(DatagramPacket dp) {
            this.ip = dp.getAddress();
            this.puerto = dp.getPort();
        }


        boolean esEste(DatagramPacket dp) {
            return ip.equals(dp.getAddress())
                && puerto == dp.getPort();
        }


        @Override
        public String toString() {
            return ip.getHostAddress() + ":" + puerto;
        }
    }
}
