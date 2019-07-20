package jetsetapp.paint;

public class SpinnerItem {
    private String mCountryName;
    private int mFlagImage;

    SpinnerItem(String countryName, int flagImage) {
        mCountryName = countryName;
        mFlagImage = flagImage;
    }

    public String getIconName() {
        return mCountryName;
    }

    public int getIconImage() {
        return mFlagImage;
    }
}
