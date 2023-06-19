package util;

/**
 * Interface to denote objects that have allocated memory that must be freed
 */
public interface Deletable {
    void delete();
}
