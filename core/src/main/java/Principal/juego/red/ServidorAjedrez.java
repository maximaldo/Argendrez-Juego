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

    private static final int MAX_CLIENTES = 2;
    private Cliente[] clientes = new Cliente[MAX_CLIENTES];


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

        if (!mensaje.equals("PING")) {
            System.out.println("[SERVER] " + datagrama.getSocketAddress() + " -> " + mensaje);
        }

        // Discovery por broadcast
        if (mensaje.equals("BUSCAR")) {
            enviarMensaje("ENCONTRAR", datagrama.getAddress(), datagrama.getPort());
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
        if (mensaje.equals("PING")) {
            enviarMensaje("PONG", datagrama.getAddress(), datagrama.getPort());
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
            clientes[0].ip, clientes[0].puerto
        );
        enviarMensaje(
            "RULETA:" + turno + "," + restante,
            clientes[1].ip, clientes[1].puerto
        );

        if (daCarta) {
            TipoCarta carta = ruleta.robarCarta();

            enviarMensaje(
                "DRAW:" + turno + "," + carta.name(),
                clientes[0].ip, clientes[0].puerto
            );
            enviarMensaje(
                "DRAW:" + turno + "," + carta.name(),
                clientes[1].ip, clientes[1].puerto
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
        if (c == clientes[0]) clientes[0] = null;
        if (c == clientes[1]) clientes[1] = null;

        // Reset del estado de partida
        turno = ColorPieza.BLANCO;
        ruleta.reset(); // ahora vemos esto

        System.out.println("[SERVER] Cliente desconectado");
    }


    private void conectarNuevoCliente(DatagramPacket dp) {

        InetAddress ip = dp.getAddress();
        int port = dp.getPort();

        //  Buscar si ya estaba conectado
        int slotExistente = buscarSlotPorDireccion(ip, port);
        if (slotExistente != -1) {
            System.out.println("[SERVER] Cliente ya conectado en slot " + slotExistente);
            return;
        }

        int slotLibre = buscarSlotLibre();
        if (slotLibre == -1) {
            enviarMensaje("Conexion denegada", ip, port);
            return;
        }

        Cliente nuevo = new Cliente(dp);
        clientes[slotLibre] = nuevo;

        if (slotLibre == 0) {
            enviarMensaje("COLOR:BLANCO", ip, port);
            System.out.println("[SERVER] BLANCAS conectado");
        } else {
            enviarMensaje("COLOR:NEGRO", ip, port);
            System.out.println("[SERVER] NEGRAS conectado");
        }

        // si ya están los dos → iniciar partida
        if (clientes[0] != null && clientes[1] != null) {
            enviarMensaje("Conexion establecida", clientes[0].ip, clientes[0].puerto);
            enviarMensaje("Conexion establecida", clientes[1].ip, clientes[1].puerto);
            partidaIniciada = true;
            System.out.println("[SERVER] Partida iniciada");
        }
    }

    private int buscarSlotPorDireccion(InetAddress ip, int puerto) {
        for (int i = 0; i < MAX_CLIENTES; i++) {
            Cliente c = clientes[i];
            if (c != null && c.ip.equals(ip) && c.puerto == puerto) {
                return i;
            }
        }
        return -1; // no encontrado
    }
    private int buscarSlotLibre() {
        for (int i = 0; i < MAX_CLIENTES; i++) {
            if (clientes[i] == null) {
                return i;
            }
        }
        return -1; // no hay lugar
    }



    private boolean conexionEstablecida() {
        return clientes[0] != null && clientes[1] != null;
    }

    private Cliente obtenerRemitente(DatagramPacket dp) {
        if (clientes[0] != null && clientes[0].esEste(dp)) return clientes[0];
        if (clientes[1] != null && clientes[1].esEste(dp)) return clientes[1];
        return null;
    }

    private Cliente obtenerOtro(Cliente emisor) {
        return emisor == clientes[0] ? clientes[1] : clientes[0];
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
