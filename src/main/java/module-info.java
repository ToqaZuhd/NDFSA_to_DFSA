module com.example.ndfsa_to_dfsa {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ndfsa_to_dfsa to javafx.fxml;
    exports com.example.ndfsa_to_dfsa;
}