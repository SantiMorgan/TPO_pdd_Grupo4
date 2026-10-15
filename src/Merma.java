public class Merma {

    private String insumo;
    private double stockTeorico;
    private double stockReal;

    public Merma(
            String insumo,
            double stockTeorico,
            double stockReal) {

        this.insumo = insumo;
        this.stockTeorico = stockTeorico;
        this.stockReal = stockReal;
    }

    public double getDiferencia() {
        return stockTeorico - stockReal;
    }

    public String getInsumo() {
        return insumo;
    }

    public double getStockTeorico() {
        return stockTeorico;
    }

    public double getStockReal() {
        return stockReal;
    }
}
