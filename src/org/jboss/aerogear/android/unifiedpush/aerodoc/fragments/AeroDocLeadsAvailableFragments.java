/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.unifiedpush.aerodoc.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.unifiedpush.aerodoc.AeroDocApplication;
import org.jboss.aerogear.android.unifiedpush.aerodoc.R;
import org.jboss.aerogear.android.unifiedpush.aerodoc.activities.AeroDocActivity;
import org.jboss.aerogear.android.unifiedpush.aerodoc.model.Lead;
import org.jboss.aerogear.android.unifiedpush.aerodoc.model.SaleAgent;

import java.util.List;

import static android.R.layout.simple_list_item_1;

public class AeroDocLeadsAvailableFragments extends Fragment {
    private static final String TAG = AeroDocLeadsAvailableFragments.class.getSimpleName();

    private AeroDocApplication application;
    private AeroDocActivity activity;
    private ListView listView;

    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private TrackLocationListener listener = new TrackLocationListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        application = (AeroDocApplication) getActivity().getApplication();
        activity = (AeroDocActivity) getActivity();

        final View view = inflater.inflate(R.layout.available_leads, null);

        listView = (ListView) view.findViewById(R.id.leads);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Lead lead = (Lead) adapterView.getItemAtPosition(position);
                displayLead(lead);
            }
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.status);

        ArrayAdapter<String> allStatus = (ArrayAdapter) spinner.getAdapter();
        int spinnerPosition = allStatus.getPosition(application.getSaleAgent().getStatus());
        spinner.setSelection(spinnerPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String status = (String) adapterView.getItemAtPosition(position);
                if (!application.getSaleAgent().getStatus().equals(status)) {
                    updateStatus(status);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        retrieveLeads();
        trackMovement();

        return view;
    }

    private void trackMovement() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(application.getApplicationContext());
        if (ConnectionResult.SUCCESS == resultCode) {
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(30000);
            locationClient = new LocationClient(application, new GooglePlayServicesClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                  locationClient.requestLocationUpdates(locationRequest, listener);
                }

                @Override
                public void onDisconnected() {
                }
            }, new GooglePlayServicesClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.e(TAG, "connection failed " + connectionResult);
                }
            });
            locationClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(listener);
        }
        super.onStop();
    }

    public void retrieveLeads() {
        final ProgressDialog dialog = activity.showProgressDialog(getString(R.string.retreving_leads));

        Pipe<Lead> pipe = application.getLeadPipe(this);
        pipe.read(new Callback<List<Lead>>() {
            @Override
            public void onSuccess(List<Lead> data) {
                ArrayAdapter<Lead> adapter = new ArrayAdapter<Lead>(activity, simple_list_item_1, data);
                listView.setAdapter(adapter);
                dialog.dismiss();
            }

            @Override
            public void onFailure(Exception e) {
                activity.displayErrorMessage(e);
                dialog.dismiss();
            }
        });
    }

    private void displayLead(final Lead lead) {
        new AlertDialog.Builder(activity)
                .setMessage(lead.getName())
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptLead(lead);
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void acceptLead(final Lead lead) {
        final ProgressDialog dialog = activity.showProgressDialog(getString(R.string.updating_lead));

        lead.setSaleAgent(application.getSaleAgent().getId());
        Pipe<Lead> leadPipe = application.getLeadPipe(this);
        leadPipe.save(lead, new Callback<Lead>() {
            @Override
            public void onSuccess(Lead data) {
                application.getLocalStore().save(lead);
                dialog.dismiss();
            }

            @Override
            public void onFailure(Exception e) {
                activity.displayErrorMessage(e);
                dialog.dismiss();
            }
        });
    }

    private void updateStatus(String status) {
        final ProgressDialog dialog = activity.showProgressDialog(getString(R.string.updating_status));

        SaleAgent saleAgent = application.getSaleAgent();
        saleAgent.setStatus(status);

        Pipe<SaleAgent> pipe = application.getSaleAgentPipe(this);
        pipe.save(saleAgent, new Callback<SaleAgent>() {
            @Override
            public void onSuccess(SaleAgent data) {
                dialog.dismiss();
            }

            @Override
            public void onFailure(Exception e) {
                activity.displayErrorMessage(e);
                dialog.dismiss();
            }
        });
    }

  private class TrackLocationListener implements LocationListener {
      @Override
      public void onLocationChanged(Location location) {
          SaleAgent saleAgent = application.getSaleAgent();
          saleAgent.setLongitude(location.getLongitude());
          saleAgent.setLatitude(location.getLatitude());

          Pipe<SaleAgent> pipe = application.getSaleAgentPipe(AeroDocLeadsAvailableFragments.this);
          pipe.save(saleAgent, new Callback<SaleAgent>() {
              @Override
              public void onFailure(Exception e) {
                  Log.e(TAG, "could not save sales agent", e);
              }

              @Override
              public void onSuccess(SaleAgent data) {
              }
          });
      }
  }
}
