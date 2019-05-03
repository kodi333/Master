package jetsetapp.paint;

public class SpinnerItem {
    private String mCountryName;
    private int mFlagImage;

    public SpinnerItem(String countryName, int flagImage) {
        mCountryName = countryName;
        mFlagImage = flagImage;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public int getFlagImage() {
        return mFlagImage;
    }
}
