package notsosig.numismaticoverhaul.villagers.data;

public interface NumismaticTradeOfferExtensions {

    void numismatic$setReputation(int reputation);

    int numismatic$getReputation();

    long numismatic$getChangeOwed();

    void numismatic$setChangeOwed(long change);
}
