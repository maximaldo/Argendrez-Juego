package Principal.juego.red;

import Principal.juego.elementos.ColorPieza;
import Principal.juego.elementos.TipoPieza;
import Principal.juego.variantes.cartas.TipoCarta;

import java.io.IOException;
import java.net.*;
import java.util.Collections;

/**
 * Cliente UDP para el ajedrez.
 * Sigue los mismos principios que HiloCliente del ejercicio de chat:
 *  - Descubre el servidor por broadcast ("Hello_there")
 *  - Usa DatagramSocket y DatagramPacket
 *  - Se conecta enviando "Conectar"
 *  - Se comunica siempre por mensajes de texto.
 */
public class ClienteAjedrez extends Thread {

    public interface ReceptorMensajes {
        void onColorAsignado(ColorPieza color);
        void onMovimientoRecibido(int sx, int sy, int dx, int dy);
        void onCartaRecibida(String data);
        void onCartaRobada(ColorPieza color, TipoCarta carta);
        void onConexionEstablecida();
        void onRuletaActualizada(ColorPieza color, int restante);
        void onPromocion(ColorPieza color, TipoPieza tipo);
        void onServidorCaido();
        void onPartidaReseteada();
    }

    private final DatagramSocket socket;
    private final InetAddress ipServer;
    private final int puertoServidor;
    private final ReceptorMensajes receptor;
    private long lastPing = 0;
    private long lastPong = System.currentTimeMillis();
    private static final long PING_INTERVAL = 1000;

    private volatile boolean fin = false;
    public volatile boolean conexionEstablecida = false;

    public ClienteAjedrez(ReceptorMensajes receptor) throws IOException {

        this.receptor = receptor;
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
            socket.setSoTimeout(1000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        InetSocketAddress servidor = descubrirServidor();
        this.ipServer = servidor.getAddress();
        this.puertoServidor = servidor.getPort();

        enviarMensajePlano("Conectar");
        System.out.println("[CLIENTE] Conectando con servidor en " + ipServer + ":" + puertoServidor);
    }

    @Override
    public void run() {
        while (!fin) {
            long now = System.currentTimeMillis();

            // enviar ping
            if (now - lastPing > PING_INTERVAL) {
                enviarMensajePlano("PING");
                lastPing = now;
            }

            try {
                byte[] buffer = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                socket.receive(dp);
                procesarMensaje(dp);
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                if (!fin) e.printStackTrace();
            }

            // si no llega pong hace 3s → server caído
            if (now - lastPong > 3000) {
                System.out.println("[CLIENTE] Servidor caído");
                if (receptor != null) {
                    receptor.onServidorCaido();
                }
                cerrar();
                break;
            }
        }

    }

    private void procesarMensaje(DatagramPacket datagrama) {
        String mensaje = new String(
            datagrama.getData(),
            0,
            datagrama.getLength()
        ).trim();

        if (!mensaje.equals("PONG")) {
            System.out.println("[CLIENTE] Recibido: " + mensaje);
        }

        if (mensaje.equals("Conexion establecida")) {
            conexionEstablecida = true;
            if (receptor != null) receptor.onConexionEstablecida();
            return;
        }
        if (mensaje.equals("PONG")) {
            lastPong = System.currentTimeMillis();
            return;
        }
        if (mensaje.startsWith("CARD:")) {
            String data = mensaje.substring("CARD:".length());
            if (receptor != null) receptor.onCartaRecibida(data);
            return;
        }
        if (mensaje.equals("RIVAL_DESCONECTADO")) {
            if (receptor != null) receptor.onServidorCaido();
            return;
        }
        if (mensaje.equals("RESET")) {
            if (receptor != null) {
                // Mejor llamarlo onPartidaReseteada
                receptor.onPartidaReseteada(); // Asegúrate de cambiar el nombre en la interfaz
            }
            // ELIMINADA LA LLAMADA A cerrar()
            return;
        }


        if (mensaje.startsWith("COLOR:")) {
            String col = mensaje.substring("COLOR:".length()).trim();
            ColorPieza c = col.equalsIgnoreCase("BLANCO")
                ? ColorPieza.BLANCO
                : ColorPieza.NEGRO;
            if (receptor != null) receptor.onColorAsignado(c);
            return;
        }

        if (mensaje.startsWith("MOVE:")) {
            String coords = mensaje.substring("MOVE:".length());
            String[] partes = coords.split(",");
            if (partes.length == 4) {
                try {
                    int sx = Integer.parseInt(partes[0]);
                    int sy = Integer.parseInt(partes[1]);
                    int dx = Integer.parseInt(partes[2]);
                    int dy = Integer.parseInt(partes[3]);
                    if (receptor != null) receptor.onMovimientoRecibido(sx, sy, dx, dy);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mensaje.startsWith("DRAW:")) {
            String[] p = mensaje.substring(5).split(",");
            ColorPieza color = ColorPieza.valueOf(p[0]);
            TipoCarta carta = TipoCarta.valueOf(p[1]);
            if (receptor != null) receptor.onCartaRobada(color, carta);
            return;
        }

        if (mensaje.startsWith("RULETA:")) {
            String[] p = mensaje.substring(7).split(",");
            ColorPieza color = ColorPieza.valueOf(p[0]);
            int restante = Integer.parseInt(p[1]);
            if (receptor != null)
                receptor.onRuletaActualizada(color, restante);
            return;
        }
        if (mensaje.startsWith("PROMO:")) {
            String[] p = mensaje.substring(6).split(",");
            ColorPieza color = ColorPieza.valueOf(p[0]);
            TipoPieza tipo = TipoPieza.valueOf(p[1]);
            if (receptor != null) receptor.onPromocion(color, tipo);
            return;
        }
    }

    public void enviarCarta(String data) {
        enviarMensajePlano("CARD:" + data);
    }

    public void enviarRoboCarta(ColorPieza color, TipoCarta carta) {
        enviarMensajePlano("DRAW:" + color + "," + carta.name());
    }
    public void enviarPromocion(ColorPieza color, TipoPieza tipo) {
        enviarMensajePlano("PROMO:" + color + "," + tipo.name());
    }

    // === API pública para el juego ===

    public void enviarMovimiento(int sx, int sy, int dx, int dy) {
        String msg = "MOVE:" + sx + "," + sy + "," + dx + "," + dy;
        enviarMensajePlano(msg);
    }

    private void enviarMensajePlano(String mensaje) {
        byte[] buffer = mensaje.getBytes();
        DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length, ipServer, puertoServidor);
        try {
            socket.send(datagrama);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cerrar() {
        enviarMensajePlano("DISCONNECT");
        fin = true;
        socket.close();
    }


    // === Descubrimiento de servidor (mismos principios que el TP de chat) ===

    private InetSocketAddress descubrirServidor() throws IOException {

        byte[] data = "BUSCAR".getBytes();
        InetAddress broadcast = obtenerBroadcast();

        DatagramPacket broadcastDatagram = new DatagramPacket(
            data,
            data.length,
            broadcast,
            4321
        );

        socket.send(broadcastDatagram);
        System.out.println("[CLIENTE] Broadcast enviado a " + broadcast.getHostAddress());

        // esperar respuesta
        byte[] buffer = new byte[1024];
        DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);
        socket.receive(respuesta);

        System.out.println("[CLIENTE] Respuesta de discovery: " +
            respuesta.getAddress() + ":" + respuesta.getPort());

        return new InetSocketAddress(
            respuesta.getAddress(),
            respuesta.getPort()
        );
    }

    private InetAddress obtenerBroadcast() throws SocketException {
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!ni.isUp() || ni.isLoopback()) continue;

            for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                InetAddress broadcast = ia.getBroadcast();
                if (broadcast != null) return broadcast;
            }
        }
        throw new RuntimeException("No se encontró broadcast");
    }
}
