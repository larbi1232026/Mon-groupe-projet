/**
 * Exception levée lors d'une erreur de lecture ou d'écriture
 * dans les opérations de persistance (fichier corrompu, format invalide, etc.).
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
