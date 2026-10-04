package Buscaminas;

import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BuscaminasFX extends Application {

    //----------------------------------------------
    // Variables de interfaz
    //----------------------------------------------
    private VBox root;
    private HBox panelSuperior;
    private Label labelEstado;
    private Button botonReiniciar;
    private GridPane tablero;
    private Button[][] botones;  // Array que contiene los 25 botones del tablero, uno por cada casilla

    //----------------------------------------------
    // Variables del juego
    //----------------------------------------------
    private boolean[][] minas;
    private boolean[][] revelado;
    private boolean[][] bandera;

    private final int TAM = 5;
    private final int NUM_MINAS = 5;

    private int casillasAbiertas = 0;

    //----------------------------------------------
    // Imágenes (así solo cargan una sola vez)
    //----------------------------------------------
    private Image imgBandera;
    private Image imgBomba;

    //----------------------------------------------
    // Colocar minas aleatorias
    //----------------------------------------------
    /**
     * Coloca las minas en posiciones aleatorias dentro del tablero.
     *
     * Se generan coordenadas aleatorias hasta alcanzar el número total de minas
     * definido en la constante NUM_MINAS.
     */
    private void inicializarMinas() {

        Random r = new Random();

        int minasColocadas = 0;

        while (minasColocadas < NUM_MINAS) {

            int f = r.nextInt(TAM);
            int c = r.nextInt(TAM);

            if (!minas[f][c]) {
                minas[f][c] = true;
                minasColocadas++;
            }
        }
    }

    //----------------------------------------------
    // Click izquierdo
    //----------------------------------------------
    /**
     * Descubre una casilla del tablero.
     *
     * Si la casilla contiene una mina, el jugador pierde la partida y se
     * muestran todas las minas. Si no contiene mina, se muestra el número de
     * minas adyacentes.
     *
     * También se controla la condición de victoria cuando todas las casillas
     * seguras han sido descubiertas.
     *
     * @param fila Fila de la casilla seleccionada.
     * @param col Columna de la casilla seleccionada.
     */
    private void descubrir(int fila, int col) {

        // Comprobamos que la casilla no está revelada y no tiene bandera
        if (!revelado[fila][col] && !bandera[fila][col]) {

            // Marcamos la casilla como revelada
            revelado[fila][col] = true;
            casillasAbiertas++;

            // Aplicamos el estilo correspondiente
            botones[fila][col].getStyleClass().add("abierta");
            botones[fila][col].setDisable(true);

            if (minas[fila][col]) {
                // Si la casilla revelada es una mina, el jugador pierde la partida
                labelEstado.setText("Estado: Has perdido");
                mostrarMinas();
                bloquearTablero();
            } else {
                // Si la casilla revelada no es una mina, mostramos el número de minas adyacentes (si las hay)
                int cantidad = contarMinas(fila, col);

                if (cantidad > 0) {
                    // Mostramos el número de minas adyacentes en el botón como texto
                    botones[fila][col].setText(String.valueOf(cantidad));
                    // Añadimos una clase CSS al botón para colorear el número según las minas adyacentes:
                    // num1 = 1 mina (azul), num2 = 2 minas (verde), num3 = 3 o más minas (rojo)
                    botones[fila][col].getStyleClass().add("num" + Math.min(cantidad, 3));

                }

                // Comprobamos si el jugador ha ganado
                if (casillasAbiertas == TAM * TAM - NUM_MINAS) {
                    labelEstado.setText("Estado: ¡Has ganado!");
                    mostrarMinas();
                    bloquearTablero();
                }
            }
        }

    }

    //----------------------------------------------
    // Click derecho (banderas)
    //----------------------------------------------
    /**
     * Coloca o elimina una bandera en la casilla seleccionada.
     *
     * Las banderas permiten al jugador marcar posibles ubicaciones de minas. No
     * se permite colocar banderas en casillas que ya han sido reveladas.
     *
     * @param fila Fila de la casilla seleccionada.
     * @param col Columna de la casilla seleccionada.
     */
    private void ponerBandera(int fila, int col) {

        // Comprobamos que la casilla no está revelada
        if (!revelado[fila][col]) {

            if (!bandera[fila][col]) {
                // Si la casilla no tiene bandera, la ponemos
                bandera[fila][col] = true;
                // Creamos una vista con la imagen de la bandera
                ImageView vista = new ImageView(imgBandera);
                vista.setFitWidth(40);
                vista.setFitHeight(40);
                botones[fila][col].setGraphic(vista);
            } else {
                // Si la casilla ya tiene bandera, se la quitamos
                bandera[fila][col] = false;
                botones[fila][col].setGraphic(null);
            }

        }
    }

    //----------------------------------------------
    // Contar minas alrededor
    //----------------------------------------------
    /**
     * Cuenta el número de minas adyacentes a una casilla determinada.
     *
     * Se recorren las ocho posiciones vecinas alrededor de la casilla indicada
     * comprobando si contienen minas.
     *
     * @param f Fila de la casilla.
     * @param c Columna de la casilla.
     * @return Número de minas alrededor de la casilla.
     */
    private int contarMinas(int f, int c) {

        int contador = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {

                int nf = f + i;
                int nc = c + j;

                if (nf >= 0 && nf < TAM && nc >= 0 && nc < TAM) {

                    if (minas[nf][nc]) {
                        contador++;
                    }

                }
            }
        }

        return contador;
    }

    //----------------------------------------------
    // Mostrar todas las minas
    //----------------------------------------------
    /**
     * Revela todas las minas del tablero.
     *
     * Este método se utiliza cuando el jugador pierde o cuando gana la partida,
     * mostrando la posición de todas las minas existentes.
     */
    private void mostrarMinas() {

        // Recorremos el tablero
        for (int fila = 0; fila < TAM; fila++) {
            for (int columna = 0; columna < TAM; columna++) {
                // Si existe una mina en la casilla recorrida, mostramos la imagen de la bomba
                if (minas[fila][columna]) {
                    botones[fila][columna].getStyleClass().remove("abierta");
                    botones[fila][columna].getStyleClass().add("mina");

                    // Creamos el ImageView con la imagen de la bomba
                    ImageView vista = new ImageView(imgBomba);
                    vista.setFitWidth(40);
                    vista.setFitHeight(40);

                    // Cargamos la imagen de la bomba en la casilla                  
                    botones[fila][columna].setGraphic(vista);
                    botones[fila][columna].setDisable(true);
                }
            }
        }

    }

    /**
     * Reinicia la partida actual.
     *
     * Se restablecen todas las estructuras de datos del juego, se limpian los
     * botones del tablero y se vuelven a colocar las minas en posiciones
     * aleatorias.
     */
    private void reiniciarJuego() {

        // Reseteamos el contador y los arrays
        casillasAbiertas = 0;
        minas = new boolean[TAM][TAM];
        revelado = new boolean[TAM][TAM];
        bandera = new boolean[TAM][TAM];

        // Recorremos el tablero y limpiamos todos los botones visualmente
        for (int fila = 0; fila < TAM; fila++) {
            for (int columna = 0; columna < TAM; columna++) {
                botones[fila][columna].setText("");
                botones[fila][columna].setGraphic(null);
                botones[fila][columna].setDisable(false);
                botones[fila][columna].getStyleClass().removeAll("abierta", "mina", "num1", "num2", "num3");
            }
        }

        // Reiniciamos el mensaje de estado
        labelEstado.setText("Estado: Jugando");

        // Colocamos nuevas minas de forma aleatoria
        inicializarMinas();
    }

    //----------------------------------------------
    // Bloquear tablero (derrota o victoria)
    //----------------------------------------------
    /**
     * Método que deshabilita todos los botones del tablero para impedir la
     * interacción del usuario con el tablero cuando la partida ha terminado.
     */
    private void bloquearTablero() {
        for (int f = 0; f < TAM; f++) {
            for (int c = 0; c < TAM; c++) {
                botones[f][c].setDisable(true);
            }
        }
    }

    /**
     * Punto de entrada de la aplicación JavaFX. Construye la interfaz gráfica,
     * inicializa el tablero y muestra la ventana.
     *
     * @param primaryStage Escenario principal proporcionado por JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {

        // Cargamos las imágenes una sola vez
        imgBandera = new Image(getClass().getResourceAsStream("bandera.png"));
        imgBomba = new Image(getClass().getResourceAsStream("bomba.png"));

        // Contenedor raíz
        root = new VBox(10);
        root.setPadding(new Insets(10));

        // Panel superior: etiqueta de estado + botón reiniciar
        labelEstado = new Label("Estado: Jugando");
        labelEstado.getStyleClass().add("estado");

        botonReiniciar = new Button("Reiniciar");
        botonReiniciar.setOnAction(event -> reiniciarJuego());

        // Creamos un elemento HBox para poner el labelEstado y el botonReiniciar en la misma fila
        panelSuperior = new HBox();
        panelSuperior.setAlignment(Pos.CENTER_LEFT);
        // El label ocupa todo el espacio disponible empujando el botón a la derecha
        HBox.setHgrow(labelEstado, Priority.ALWAYS);
        labelEstado.setMaxWidth(Double.MAX_VALUE);
        // Añadimos los elementos al panel superior
        panelSuperior.getChildren().addAll(labelEstado, botonReiniciar);

        // Añadimos el panel superior al VBox
        root.getChildren().add(panelSuperior);

        // Creamos el tablero GridPane
        tablero = new GridPane();
        tablero.setHgap(4);  // separación horizontal entre casillas
        tablero.setVgap(4);  // separación vertical entre casillas

        // Inicializamos el array de botones y las matrices del juego
        botones = new Button[TAM][TAM];
        minas = new boolean[TAM][TAM];
        revelado = new boolean[TAM][TAM];
        bandera = new boolean[TAM][TAM];

        // Colocamos las minas aleatoriamente
        inicializarMinas();

        // Recorremos el tablero, fila por fila y columna por columna
        // Creando así, los 25 botones del tablero
        for (int fila = 0; fila < TAM; fila++) {
            for (int columna = 0; columna < TAM; columna++) {
                // Añadimos un botón por cada iteración
                Button boton = new Button();
                boton.setPrefSize(60, 60);  // tamaño de cada casilla
                boton.setMinSize(60, 60);
                boton.setMaxSize(60, 60);
                boton.getStyleClass().add("casilla");

                // Guardamos el contenido de las variables fila y columna para poder usarlas dentro de la expresión lambda
                final int f = fila;
                final int c = columna;

                // Añadimos el manejador de eventos del ratón para las funciones descubrir y ponerBandera
                // Usaremos una expresión lambda para simplificar el código
                boton.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        descubrir(f, c);
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        ponerBandera(f, c);
                    }
                });

                botones[fila][columna] = boton; // guardamos el botón en el array
                tablero.add(boton, columna, fila); // añadimos al grid (columna, fila)

            }
        }

        // Añadimos el tablero al elemento raíz
        root.getChildren().add(tablero);

        // Creamos la escena con la caja VBox como elemento raíz con un tamaño predeterminado
        Scene scene = new Scene(root, 340, 400);
        // Enlazamos con el archivo CSS para que se apliquen los estilos correspondientes
        scene.getStylesheets().add(getClass().getResource("Buscaminas.css").toExternalForm());

        // Añadimos el icono y el título a la ventana del juego y que la ventana no sea redimensionable
        Image logo = new Image(getClass().getResourceAsStream("logoBuscaminas.png"));
        primaryStage.getIcons().add(logo);
        primaryStage.setTitle("Buscaminas");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);

        // Mostramos el contenido de la escena
        primaryStage.show();

    }

    /**
     * Método main. Lanza la aplicación JavaFX.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }

}
