package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import com.owlike.genson.Genson;
import java.util.List;
import org.hyperledger.fabric.shim.ledger.KeyModification;

@Contract(name = "AssetContract")
public class AssetContract implements ContractInterface {
    private final Genson genson = new Genson();

    @Transaction()
    public void createAsset(Context ctx, String assetId, String dealerId, String msisdn, String mpin,
                            double balance, String status, double transAmount, String transType, String remarks) {
        ChaincodeStub stub = ctx.getStub();
        Asset asset = new Asset();
        asset.setDealerId(dealerId);
        asset.setMsisdn(msisdn);
        asset.setMpin(mpin);
        asset.setBalance(balance);
        asset.setStatus(status);
        asset.setTransAmount(transAmount);
        asset.setTransType(transType);
        asset.setRemarks(remarks);

        String assetJSON = genson.serialize(asset);
        stub.putStringState(assetId, assetJSON);
    }

    @Transaction()
    public Asset readAsset(Context ctx, String assetId) {
        String assetJSON = ctx.getStub().getStringState(assetId);
        if (assetJSON == null || assetJSON.isEmpty()) {
            throw new RuntimeException("Asset " + assetId + " does not exist");
        }
        return genson.deserialize(assetJSON, Asset.class);
    }

    @Transaction()
    public void updateAssetBalance(Context ctx, String assetId, double newBalance) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetId);
        if (assetJSON == null || assetJSON.isEmpty()) {
            throw new RuntimeException("Asset " + assetId + " does not exist");
        }
        Asset asset = genson.deserialize(assetJSON, Asset.class);
        asset.setBalance(newBalance);
        stub.putStringState(assetId, genson.serialize(asset));
    }

    @Transaction()
    public String getAssetHistory(Context ctx, String assetId) {
        ChaincodeStub stub = ctx.getStub();
        StringBuilder history = new StringBuilder();
        List<KeyModification> modifications = stub.getHistoryForKey(assetId);
        for (KeyModification mod : modifications) {
            history.append(mod.getStringValue()).append("\n");
        }
        return history.toString();
    }
}
