package gymfit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class GymFitController implements Initializable {

    // ---- Formulario ----
    @FXML private TextField txtNombre, txtApellidos, txtIban, txtBuscar;
    @FXML private DatePicker dpNacimiento;
    @FXML private ToggleGroup tgSexo, tgCuota;
    @FXML private ComboBox<String> cbProvincia, cbClase;
    @FXML private CheckBox chkDatos, chkNovedades;
    @FXML private Label lblError, lblEstado;

    // ---- Tabla ----
    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, String> colNombre, colApellidos, colNacimiento, colProvincia, colCuota, colClase;

    private final ObservableList<Socio> socios = FXCollections.observableArrayList();
    private FilteredList<Socio> filtrados;
    private Socio socioEditando;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] PROVINCIAS = {
        "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz", "Barcelona", "Burgos",
        "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real", "Córdoba", "A Coruña", "Cuenca", "Girona",
        "Granada", "Guadalajara", "Guipúzcoa", "Huelva", "Huesca", "Islas Baleares", "Jaén", "León", "Lleida",
        "Lugo", "Madrid", "Málaga", "Murcia", "Navarra", "Ourense", "Palencia", "Las Palmas", "Pontevedra",
        "La Rioja", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", "Tarragona", "Teruel",
        "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza", "Ceuta", "Melilla"
    };

    private static final String[] CLASES = {
        "Spinning", "Yoga", "Pilates", "Zumba", "CrossFit", "Natación", "Body Pump", "HIIT", "Boxeo", "Aquagym", "Stretching", "Funcional"
    };

    /** Modelo de socio. */
    public static class Socio {
        private final String nombre, apellidos, nacimiento, sexo, provincia, cuota, clase, iban;

        public Socio(String nombre, String apellidos, String nacimiento, String sexo,
                     String provincia, String cuota, String clase, String iban) {
            this.nombre = nombre; this.apellidos = apellidos; this.nacimiento = nacimiento; this.sexo = sexo;
            this.provincia = provincia; this.cuota = cuota; this.clase = clase; this.iban = iban;
        }
        public String getNombre() { return nombre; }
        public String getApellidos() { return apellidos; }
        public String getNacimiento() { return nacimiento; }
        public String getSexo() { return sexo; }
        public String getProvincia() { return provincia; }
        public String getCuota() { return cuota; }
        public String getClase() { return clase; }
        public String getIban() { return iban; }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.managedProperty().bind(lblError.visibleProperty());

        cbProvincia.setItems(FXCollections.observableArrayList(PROVINCIAS));
        cbClase.setItems(FXCollections.observableArrayList(CLASES));

        // DatePicker en formato dd/mm/aaaa
        dpNacimiento.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate d) { return d == null ? "" : d.format(FMT); }
            @Override public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) return null;
                try { return LocalDate.parse(s.trim(), FMT); }
                catch (DateTimeParseException e) { return null; }
            }
        });

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colNacimiento.setCellValueFactory(new PropertyValueFactory<>("nacimiento"));
        colProvincia.setCellValueFactory(new PropertyValueFactory<>("provincia"));
        colCuota.setCellValueFactory(new PropertyValueFactory<>("cuota"));
        colClase.setCellValueFactory(new PropertyValueFactory<>("clase"));

        socios.add(new Socio("Ana", "Pérez López", "12/03/1999", "Mujer", "Sevilla", "Plus", "Yoga", ""));
        socios.add(new Socio("Luis", "Martín Gil", "21/07/2001", "Hombre", "Sevilla", "Básica", "Spinning", ""));
        socios.add(new Socio("Marta", "Ruiz Soto", "30/11/1988", "Mujer", "Córdoba", "Premium", "Pilates", ""));
        socios.add(new Socio("José", "Navarro Díaz", "02/02/2005", "Hombre", "Málaga", "Joven", "CrossFit", ""));

        filtrados = new FilteredList<>(socios, s -> true);
        txtBuscar.textProperty().addListener((obs, anterior, texto) -> filtrados.setPredicate(s ->
                texto == null || texto.isBlank()
                        || (s.getNombre() + " " + s.getApellidos()).toLowerCase().contains(texto.toLowerCase().trim())));
        tablaSocios.setItems(filtrados);
    }

    // ================= GUARDAR / VALIDAR =================

    @FXML
    private void onGuardar() {
        String error = validar();
        if (error != null) {
            lblError.setText(error);
            lblError.setVisible(true);
            lblEstado.setText("No se ha guardado el socio: revisa el mensaje del formulario.");
            return;
        }
        lblError.setVisible(false);

        Socio nuevo = new Socio(
                txtNombre.getText().trim(), txtApellidos.getText().trim(),
                dpNacimiento.getValue().format(FMT), textoSeleccionado(tgSexo),
                cbProvincia.getValue(), textoSeleccionado(tgCuota), cbClase.getValue(), txtIban.getText().trim());

        if (socioEditando != null) {
            int i = socios.indexOf(socioEditando);
            socios.set(i, nuevo);
            lblEstado.setText("Socio actualizado: " + nuevo.getNombre() + " " + nuevo.getApellidos());
            socioEditando = null;
        } else {
            socios.add(nuevo);
            lblEstado.setText("Socio guardado correctamente: " + nuevo.getNombre() + " " + nuevo.getApellidos());
        }
        vaciarFormulario();
    }

    /** Devuelve un mensaje (qué ha pasado + cómo solucionarlo) o null si todo es válido. */
    private String validar() {
        if (txtNombre.getText().isBlank()) {
            txtNombre.requestFocus();
            return "Falta el nombre del socio.\nEscríbelo en el campo «Nombre» (ej.: Laura).";
        }
        if (txtApellidos.getText().isBlank()) {
            txtApellidos.requestFocus();
            return "Faltan los apellidos del socio.\nEscríbelos en el campo «Apellidos» (ej.: Gómez Ruiz).";
        }
        LocalDate fecha = dpNacimiento.getConverter().fromString(dpNacimiento.getEditor().getText());
        if (fecha == null && dpNacimiento.getValue() == null) {
            dpNacimiento.requestFocus();
            return "La fecha de nacimiento no es válida.\nElige el día en el calendario o escríbela como dd/mm/aaaa (ej.: 14/05/2003).";
        }
        if (fecha != null) dpNacimiento.setValue(fecha);
        if (dpNacimiento.getValue().isAfter(LocalDate.now())) {
            dpNacimiento.requestFocus();
            return "La fecha de nacimiento es posterior a hoy.\nRevisa el año e introduce una fecha pasada (ej.: 14/05/2003).";
        }
        if (cbProvincia.getValue() == null) {
            cbProvincia.requestFocus();
            return "Falta la provincia.\nElígela en el desplegable «Provincia» (ej.: Sevilla).";
        }
        if (tgCuota.getSelectedToggle() == null) {
            return "Falta elegir el tipo de cuota.\nMarca una opción: Básica, Plus, Premium, Joven o Senior.";
        }
        if (!txtIban.getText().isBlank() && !txtIban.getText().replace(" ", "").matches("[A-Z]{2}\\d{2}[A-Z0-9]{10,30}")) {
            txtIban.requestFocus();
            return "El IBAN no tiene un formato válido.\nEscríbelo con dos letras y números, como ES91 2100 0418 4502 0005 1332, o déjalo vacío.";
        }
        if (!chkDatos.isSelected()) {
            return "Hay que aceptar el tratamiento de datos para dar de alta al socio.\nMarca la casilla «Acepto el tratamiento de mis datos».";
        }
        return null;
    }

    // ================= LIMPIAR (destructiva, con confirmación) =================

    @FXML
    private void onLimpiar() {
        ButtonType limpiar = new ButtonType("Limpiar", ButtonBar.ButtonData.OK_DONE);
        ButtonType mantener = new ButtonType("Mantener datos", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Se borrarán todos los datos que has escrito en el formulario.", limpiar, mantener);
        c.setTitle("Limpiar formulario");
        c.setHeaderText("¿Quieres limpiar el formulario?");
        c.showAndWait().filter(b -> b == limpiar).ifPresent(b -> {
            vaciarFormulario();
            socioEditando = null;
            lblEstado.setText("Formulario limpio.");
        });
    }

    // ================= ACCIONES SOBRE SOCIOS =================

    @FXML
    private void onBaja() {
        Socio s = tablaSocios.getSelectionModel().getSelectedItem();
        if (s == null) { avisarSeleccion(); return; }
        ButtonType baja = new ButtonType("Dar de baja", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Se eliminará la ficha de " + s.getNombre() + " " + s.getApellidos() + ".\nEsta acción no se puede deshacer.",
                baja, cancelar);
        c.setTitle("Dar de baja");
        c.setHeaderText("¿Seguro que quieres dar de baja a este socio?");
        c.showAndWait().filter(b -> b == baja).ifPresent(b -> {
            socios.remove(s);
            lblEstado.setText("Socio dado de baja: " + s.getNombre() + " " + s.getApellidos());
        });
    }

    @FXML
    private void onEditar() {
        Socio s = tablaSocios.getSelectionModel().getSelectedItem();
        if (s == null) { avisarSeleccion(); return; }
        socioEditando = s;
        txtNombre.setText(s.getNombre());
        txtApellidos.setText(s.getApellidos());
        dpNacimiento.setValue(LocalDate.parse(s.getNacimiento(), FMT));
        seleccionarPorTexto(tgSexo, s.getSexo());
        cbProvincia.setValue(s.getProvincia());
        seleccionarPorTexto(tgCuota, s.getCuota());
        cbClase.setValue(s.getClase());
        txtIban.setText(s.getIban());
        chkDatos.setSelected(true);
        lblError.setVisible(false);
        lblEstado.setText("Editando a " + s.getNombre() + " " + s.getApellidos() + ". Pulsa «Guardar socio» para aplicar los cambios.");
        txtNombre.requestFocus();
    }

    @FXML
    private void onDuplicar() {
        Socio s = tablaSocios.getSelectionModel().getSelectedItem();
        if (s == null) { avisarSeleccion(); return; }
        socios.add(new Socio(s.getNombre(), s.getApellidos(), s.getNacimiento(), s.getSexo(),
                s.getProvincia(), s.getCuota(), s.getClase(), s.getIban()));
        lblEstado.setText("Socio duplicado: " + s.getNombre() + " " + s.getApellidos());
    }

    @FXML private void onRenovar()   { accionPrototipo("Renovar cuota"); }
    @FXML private void onCongelar()  { accionPrototipo("Congelar socio"); }
    @FXML private void onHistorial() { accionPrototipo("Ver historial"); }
    @FXML private void onImprimir()  { accionPrototipo("Imprimir listado"); }
    @FXML private void onPendiente() { lblEstado.setText("Esta opción no está incluida en el prototipo."); }

    // ================= OTRAS ACCIONES =================

    @FXML
    private void onNuevo() {
        vaciarFormulario();
        socioEditando = null;
        lblError.setVisible(false);
        lblEstado.setText("Listo para dar de alta un socio nuevo.");
        txtNombre.requestFocus();
    }

    @FXML private void onBuscar()     { txtBuscar.requestFocus(); txtBuscar.selectAll(); }
    @FXML private void onActualizar() { tablaSocios.refresh(); lblEstado.setText("Lista actualizada."); }
    @FXML private void onSalir()      { Platform.exit(); }

    // ================= AUXILIARES =================

    private void accionPrototipo(String nombre) {
        Socio s = tablaSocios.getSelectionModel().getSelectedItem();
        if (s == null && !nombre.startsWith("Imprimir")) { avisarSeleccion(); return; }
        lblEstado.setText(nombre + (s != null ? ": " + s.getNombre() + " " + s.getApellidos() : "")
                + " (acción de demostración del prototipo).");
    }

    private void avisarSeleccion() {
        lblEstado.setText("Selecciona antes un socio de la tabla.");
    }

    private void vaciarFormulario() {
        txtNombre.clear(); txtApellidos.clear(); txtIban.clear();
        dpNacimiento.setValue(null);
        dpNacimiento.getEditor().clear();
        tgSexo.selectToggle(null);
        tgCuota.selectToggle(null);
        cbProvincia.setValue(null);
        cbClase.setValue(null);
        chkDatos.setSelected(false);
        chkNovedades.setSelected(false);
    }

    private String textoSeleccionado(ToggleGroup g) {
        Toggle t = g.getSelectedToggle();
        return t == null ? "" : ((RadioButton) t).getText();
    }

    private void seleccionarPorTexto(ToggleGroup g, String texto) {
        g.getToggles().stream()
                .filter(t -> ((RadioButton) t).getText().equals(texto))
                .findFirst().ifPresentOrElse(g::selectToggle, () -> g.selectToggle(null));
    }
}
