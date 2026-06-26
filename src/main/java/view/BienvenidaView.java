package view;

import controller.AutenticacionController;
import model.InicioSesion;
import model.Usuario;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 * CAPA: VIEW — Vista de Bienvenida
 * ============================================================
 * Pantalla final del flujo de autenticación.
 * Si el usuario es ADMIN, muestra el botón "Panel Admin".
 *
 * Clase: BienvenidaView
 * Módulo: Gestión de Usuarios y Autenticación — QoriBank
 * ============================================================
 */
public class BienvenidaView extends JFrame {

    private static final Color DORADO_PRINCIPAL = LoginView.DORADO_PRINCIPAL;
    private static final Color DORADO_OSCURO    = LoginView.DORADO_OSCURO;
    private static final Color DORADO_CLARO     = LoginView.DORADO_CLARO;
    private static final Color GRIS_FONDO       = LoginView.GRIS_FONDO;
    private static final Color BLANCO           = LoginView.BLANCO;
    private static final Color TEXTO_OSCURO     = LoginView.TEXTO_OSCURO;
    private static final Color VERDE_ACTIVO     = new Color(0x27AE60);
    private static final Color AZUL_ADMIN       = new Color(0x2980B9);

    private final AutenticacionController controller;

    public BienvenidaView(AutenticacionController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Qori Bank — Bienvenido");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        Usuario      usuario = controller.getUsuarioActual();
        InicioSesion ses     = controller.getSesionActual();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GRIS_FONDO);
        setContentPane(root);

        root.add(crearBarraLateral(usuario), BorderLayout.WEST);
        root.add(crearPanelBienvenida(usuario, ses), BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // Barra lateral
    // --------------------------------------------------------
    private JPanel crearBarraLateral(Usuario usuario) {
        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint grad = new GradientPaint(
                        0, 0, DORADO_PRINCIPAL, 0, getHeight(), DORADO_OSCURO);
                g2.setPaint(grad);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        barra.setPreferredSize(new Dimension(260, 0));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(BorderFactory.createEmptyBorder(40, 25, 30, 25));

        JLabel lblLogo = new JLabel("Qori Bank");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(BLANCO);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblLogo);

        barra.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel avatar = crearAvatar(usuario.getPrimerNombre(), 70);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(avatar);

        barra.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel lblNombre = new JLabel(
                "<html><center>" + usuario.getNombreCompleto() + "</center></html>");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNombre.setForeground(BLANCO);
        lblNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblNombre.setPreferredSize(new Dimension(210, 40));
        barra.add(lblNombre);

        barra.add(Box.createRigidArea(new Dimension(0, 6)));

        // Badge de rol
        JLabel lblRol = crearBadgeRol(usuario.getRol());
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblRol);

        barra.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel lblActivo = crearBadgeActivo();
        lblActivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblActivo);

        barra.add(Box.createVerticalGlue());

        // Módulos del menú
        JLabel lblInicio = crearItemMenu("🏠  Inicio");
        barra.add(lblInicio);
        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        // "Mis Cuentas" es clickeable solo para CLIENTE (no ADMIN)
        if (!usuario.esAdmin()) {
            JButton btnMisCuentas = crearBotonBarra("💳  Mis Cuentas", new Color(0x6C5CE7));
            btnMisCuentas.addActionListener(e -> abrirMisCuentas());
            btnMisCuentas.setAlignmentX(Component.CENTER_ALIGNMENT);
            barra.add(btnMisCuentas);
            barra.add(Box.createRigidArea(new Dimension(0, 8)));
        } else {
            JLabel lblMisCuentas = crearItemMenu("💳  Mis Cuentas");
            barra.add(lblMisCuentas);
            barra.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // "Transferencias" es clickeable solo para CLIENTE (no ADMIN)
        if (!usuario.esAdmin()) {
            JButton btnTransferencias = crearBotonBarra("↗  Transferencias", new Color(0x00897B));
            btnTransferencias.addActionListener(e -> abrirTransferencias());
            btnTransferencias.setAlignmentX(Component.CENTER_ALIGNMENT);
            barra.add(btnTransferencias);
            barra.add(Box.createRigidArea(new Dimension(0, 8)));
        } else {
            JLabel lblTransferencias = crearItemMenu("↗  Transferencias");
            barra.add(lblTransferencias);
            barra.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JLabel lblNotificaciones = crearItemMenu("🔔  Notificaciones");
        barra.add(lblNotificaciones);
        barra.add(Box.createRigidArea(new Dimension(0, 10)));

        barra.add(Box.createRigidArea(new Dimension(0, 6)));

        // Botón Panel Admin (solo para ADMIN)
        if (usuario.esAdmin()) {
            JButton btnAdmin = crearBotonBarra("⚙️  Panel Admin", AZUL_ADMIN);
            btnAdmin.addActionListener(e -> abrirPanelAdmin());
            btnAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
            barra.add(btnAdmin);
            barra.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Botón Cerrar Sesión
        JButton btnCerrar = new JButton("Cerrar Sesión");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setForeground(DORADO_OSCURO);
        btnCerrar.setBackground(BLANCO);
        btnCerrar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BLANCO, 1, true),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrar.setMaximumSize(new Dimension(180, 34));
        btnCerrar.addActionListener(e -> cerrarSesion());
        barra.add(btnCerrar);

        return barra;
    }

    // --------------------------------------------------------
    // Panel principal de bienvenida
    // --------------------------------------------------------
    private JPanel crearPanelBienvenida(Usuario usuario, InicioSesion sesion) {
        JPanel panelOuter = new JPanel(new GridBagLayout());
        panelOuter.setBackground(GRIS_FONDO);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblSaludo = new JLabel(
                "¡Bienvenido, " + usuario.getPrimerNombre() + "! 👋");
        lblSaludo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblSaludo.setForeground(TEXTO_OSCURO);
        lblSaludo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblSaludo);

        JLabel lblFecha = new JLabel("Sesión iniciada el " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")));
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFecha.setForeground(Color.GRAY);
        lblFecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(lblFecha);

        contenido.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel tarjeta = crearTarjetaPerfil(usuario, sesion);
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(tarjeta);

        contenido.add(Box.createRigidArea(new Dimension(0, 20)));

        // Panel de estado de sesión (STATE)
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelEstado.setBackground(new Color(0xEAF7EE));
        panelEstado.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VERDE_ACTIVO, 1, true),
                BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        panelEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lblEstadoIcono = new JLabel("✅");
        lblEstadoIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        panelEstado.add(lblEstadoIcono);

        String estadoTexto = sesion != null
                ? "Sesión " + sesion.getEstado().name()
                  + " — autenticación de dos factores completada."
                : "Sesión VERIFICADA — autenticación completada.";
        JLabel lblEstado = new JLabel(estadoTexto);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstado.setForeground(new Color(0x1A6B3A));
        panelEstado.add(lblEstado);

        panelEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(panelEstado);

        panelOuter.add(contenido);
        return panelOuter;
    }

    // --------------------------------------------------------
    // Tarjeta de perfil con fila "Rol"
    // --------------------------------------------------------
    private JPanel crearTarjetaPerfil(Usuario usuario, InicioSesion sesion) {
        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLANCO);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(DORADO_PRINCIPAL);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 6, 6, 6));
                g2.dispose();
            }
        };
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setOpaque(false);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 8, 6, 24);

        JLabel lblTitulo = new JLabel("Información del Perfil");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(TEXTO_OSCURO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 8, 14, 8);
        tarjeta.add(lblTitulo, gbc);
        gbc.gridwidth = 1;

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0xE0E0E0));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 8, 14, 8);
        tarjeta.add(sep, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        String[][] datos = {
            {"👤  Nombre completo:", usuario.getNombreCompleto()},
            {"✉️  Correo:",          usuario.getEmail()},
            {"📱  Teléfono:",        usuario.getTelefono()},
            {"🔑  Rol:",             usuario.getRol()},
            {"🔒  ID de Usuario:",   "#" + usuario.getIdUsuario()},
            {"🛡️  Canal 2FA:",       sesion != null ? sesion.getCanal().getNombre() : "N/A"}
        };

        int fila = 2, col = 0;
        for (String[] par : datos) {
            gbc.insets = new Insets(5, 8, 5, 4);
            JLabel lbl = new JLabel(par[0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Color.GRAY);
            gbc.gridx = col * 2; gbc.gridy = fila;
            tarjeta.add(lbl, gbc);

            gbc.insets = new Insets(5, 0, 5, 24);
            JLabel val = new JLabel(par[1]);
            val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            val.setForeground(TEXTO_OSCURO);
            gbc.gridx = col * 2 + 1;
            tarjeta.add(val, gbc);

            col++;
            if (col == 2) { col = 0; fila++; }
        }
        return tarjeta;
    }

    // --------------------------------------------------------
    // Evento: abrir panel de administración
    // --------------------------------------------------------
    private void abrirPanelAdmin() {
        new AdminView(controller).setVisible(true);
        this.setVisible(false);
    }

    // --------------------------------------------------------
    // Evento: abrir "Mis Cuentas" (solo CLIENTE)
    // --------------------------------------------------------
    private void abrirMisCuentas() {
        new MisCuentasView(controller).setVisible(true);
        this.setVisible(false);
    }

    // --------------------------------------------------------
    // Evento: abrir "Transferencias" (solo CLIENTE)
    // --------------------------------------------------------
    private void abrirTransferencias() {
        new TransferenciasView(controller).setVisible(true);
        this.setVisible(false);
    }

    // --------------------------------------------------------
    // Cerrar sesión
    // --------------------------------------------------------
    private void cerrarSesion() {
        int op = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Cerrar Sesión — Qori Bank",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (op == JOptionPane.YES_OPTION) {
            controller.cerrarSesion();
            new LoginView().setVisible(true);
            this.dispose();
        }
    }

    // --------------------------------------------------------
    // Helpers de UI
    // --------------------------------------------------------
    private JPanel crearAvatar(String nombre, int tam) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillOval(0, 0, tam, tam);
                g2.setColor(BLANCO);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(1, 1, tam - 2, tam - 2);
                g2.setColor(BLANCO);
                g2.setFont(new Font("Segoe UI", Font.BOLD, tam / 2));
                String inicial = (nombre != null && !nombre.isEmpty())
                        ? String.valueOf(nombre.charAt(0)).toUpperCase() : "?";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(inicial,
                        (tam - fm.stringWidth(inicial)) / 2,
                        (tam + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(tam, tam));
        panel.setMaximumSize(new Dimension(tam, tam));
        panel.setOpaque(false);
        return panel;
    }

    private JLabel crearBadgeActivo() {
        JLabel badge = new JLabel("  ● ACTIVO  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VERDE_ACTIVO);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(BLANCO);
        badge.setOpaque(false);
        return badge;
    }

    private JLabel crearBadgeRol(String rol) {
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        Color color = esAdmin ? AZUL_ADMIN : new Color(0x7F8C8D);
        JLabel badge = new JLabel("  " + rol + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(BLANCO);
        badge.setOpaque(false);
        return badge;
    }

    private JButton crearBotonBarra(String texto, Color color) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? color.brighter() : color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(BLANCO);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setMaximumSize(new Dimension(180, 34));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel crearItemMenu(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(255, 255, 255, 180));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }
}
