package service;

public class BankSession implements AutoCloseable {
    private boolean closed;

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            System.out.println("Money Bank session closed.");
        }
    }
}
