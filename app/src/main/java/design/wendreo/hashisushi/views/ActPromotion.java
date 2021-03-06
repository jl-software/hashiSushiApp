package design.wendreo.hashisushi.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import design.wendreo.hashisushi.R;
import design.wendreo.hashisushi.adapter.AdapterProduct;
import design.wendreo.hashisushi.dao.UserFirebase;
import design.wendreo.hashisushi.listener.RecyclerItemClickListener;
import design.wendreo.hashisushi.model.OrderItens;
import design.wendreo.hashisushi.model.Orders;
import design.wendreo.hashisushi.model.Product;
import design.wendreo.hashisushi.model.User;
import design.wendreo.hashisushi.views.cardap.ActAdditional;
import design.wendreo.hashisushi.views.cardap.ActCombo;
import design.wendreo.hashisushi.views.cardap.ActDrinks;
import design.wendreo.hashisushi.views.cardap.ActPlatAce;
import design.wendreo.hashisushi.views.cardap.ActPlatHot;
import design.wendreo.hashisushi.views.cardap.ActPortions;
import design.wendreo.hashisushi.views.cardap.ActSaleCardap;
import design.wendreo.hashisushi.views.cardap.ActTemakis;
import design.wendreo.hashisushi.views.policyPrivacy.ActPolicyPrivacy;
import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
/*
* DO NOT remove microsoft app center below
 */
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class ActPromotion extends AppCompatActivity implements View.OnClickListener {
	public static String STATUS = null;
	private TextView txtQuantItens;
	private TextView txtTotalOrder;
	private TextView txtPromution;
	private TextView txtStatus;
	private FloatingActionButton flotBntSalesCardap;
	private FloatingActionButton flotBntFinishProm;
	private FloatingActionButton flotBntExitP;
	
	private DatabaseReference reference;
	private List< Product > productsList = new ArrayList< Product > ( );
	private List< OrderItens > itensCars = new ArrayList<> ( );
	private RecyclerView list_produsts;
	private AdapterProduct adapterProduct;
	private AlertDialog dialog;
	private String retornIdUser;
	private User user;
	private FirebaseAuth auth;
	private Orders ordersRecovery;
	
	private int qtdItensCar;
	private Double totalCar;
	private int pontos;
	
	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.act_promotion );
		
		ActionBar bar = getSupportActionBar ( );
		bar.setBackgroundDrawable ( new ColorDrawable ( Color.parseColor ( "#000000" ) ) );
		bar.setTitle ( "" );
		
		//Travæ rotaçãø da tela
		setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
		
		getDate ( );
		startComponet ( );
		initDB ( );
		retornIdUser = UserFirebase.getIdUser ( );
		getStatus ( );
		fontLogo ( );
		
		recyclerViewConfig ( );
		recycleOnclick ( );
		
		initSearch ( );
		recoveryDataUser ( );
		this.auth = FirebaseAuth.getInstance ( );
		
		listesnerEventPedidos ( );//escula pedidos
		
		//microsoft app center DO NO REMOVE
		AppCenter.start ( getApplication ( ), "da48292b-6c75-4397-bfea-56eefa9bb546",
				Analytics.class, Crashes.class );
	}//end onCreate
	
	//finaliza se voltar
	@Override
	public void onBackPressed ( ) {
		finish ( );
	}
	
	//Altera fonte do txtLogo
	private void fontLogo ( ) {
		
		Typeface font = Typeface.createFromAsset ( getAssets ( ), "RagingRedLotusBB.ttf" );
		txtPromution.setTypeface ( font );
		txtStatus.setTypeface ( font );
	}
	
	private void recycleOnclick ( ) {
		//Adiciona evento de clique no recyclerview
		list_produsts.addOnItemTouchListener (
				
				new RecyclerItemClickListener (
						this,
						list_produsts,
						new RecyclerItemClickListener.OnItemClickListener ( ) {
							@Override
							public void onItemClick ( View view, int position ) {
								Product produtoSelecionado = productsList.get ( position );
								confirmItem ( position, produtoSelecionado );
							}
							
							@Override
							public void onLongItemClick ( View view, int position ) {
								//Product produtoSelecionado = productsList.get(position);
								// msgShort("Produto :"+produtoSelecionado);
							}
							
							@Override
							public void onItemClick ( AdapterView< ? > parent, View view, int position, long id ) {
							
							}
							
						}
				)
		);
	}
	
	//Configura recyclerview
	private void recyclerViewConfig ( ) {
		
		list_produsts.setLayoutManager ( new LinearLayoutManager ( this ) );
		list_produsts.setHasFixedSize ( true );
		adapterProduct = new AdapterProduct ( productsList, this );
		list_produsts.setAdapter ( adapterProduct );
	}
	
	private void startComponet ( ) {
		txtStatus = findViewById ( R.id.txtEstatus );
		txtPromution = findViewById ( R.id.txtPromution );
		txtQuantItens = findViewById ( R.id.txtQuantItens );
		txtTotalOrder = findViewById ( R.id.txtTotalOrder );
		
		flotBntExitP = findViewById ( R.id.flotBntExitP );
		flotBntFinishProm = findViewById ( R.id.flotBntFinishProm );
		flotBntSalesCardap = findViewById ( R.id.flotBntSalesCardap );
		//RecyclerView---
		list_produsts = findViewById ( R.id.list_produsts );
		
		flotBntExitP.setOnClickListener ( this );
		flotBntFinishProm.setOnClickListener ( this );
		flotBntSalesCardap.setOnClickListener ( this );
	}
	
	private void getStatus ( ) {
		
		if ( STATUS.equals ( getString ( R.string.we_are_open_now ) ) ) {
			txtStatus.setTextColor ( Color.GREEN );
		} else {
			txtStatus.setTextColor ( Color.RED );
		}
		txtStatus.setText ( STATUS );
	}
	
	@Override
	protected void attachBaseContext ( Context newBase ) {
		super.attachBaseContext ( CalligraphyContextWrapper.wrap ( newBase ) );
	}
	
	@Override
	public void onClick ( View v ) {
		if ( v.getId ( ) == R.id.flotBntSalesCardap ) {
			startVibrate ( 90 );
			openSaleCardap ( );
		}
		if ( v.getId ( ) == R.id.flotBntFinishProm ) {
			startVibrate ( 90 );
			Intent it = new Intent ( this, ActOrder.class );
			startActivity ( it );
		} else if ( v.getId ( ) == R.id.flotBntExitP ) {
			startVibrate ( 90 );
			//finaliza a activity atual e todas a baixo
			auth.signOut ( );
			this.finishAffinity ( );
			
		}
	}
	
	//Metudo que ativa vibração
	public void startVibrate ( long time ) {
		// cria um obj atvib que recebe seu valor de context
		Vibrator atvib = ( Vibrator ) getSystemService ( Context.VIBRATOR_SERVICE );
		atvib.vibrate ( time );
	}
	
	private void openSaleCardap ( ) {
		Intent intent = new Intent ( ActPromotion.this, ActSaleCardap.class );
		//Passa efeitos de transzição
		ActivityOptionsCompat actcompat = ActivityOptionsCompat.makeCustomAnimation ( getApplicationContext ( ),
				R.anim.fade_in, R.anim.mover_direita );
		ActivityCompat.startActivity ( ActPromotion.this, intent, actcompat.toBundle ( ) );
		//startActivity(intent);
	}
	
	public void initDB ( ) {
		FirebaseApp.initializeApp ( ActPromotion.this );
		this.reference = FirebaseDatabase.getInstance ( ).getReference ( );
	}
	
	public void initSearch ( ) {
		//retorna produto
		DatabaseReference productDB = reference.child ( "product" );
		//retorna tipo setado
		Query querySearch = productDB.orderByChild ( "promotion" ).equalTo ( true );
		
		//cria um ouvinte
		querySearch.addValueEventListener ( new ValueEventListener ( ) {
			@Override
			public void onDataChange ( @NonNull DataSnapshot dataSnapshot ) {
				for ( DataSnapshot objSnapshot : dataSnapshot.getChildren ( ) ) {
					Product p = objSnapshot.getValue ( Product.class );
					productsList.add ( p );
				}
				adapterProduct.notifyDataSetChanged ( );
			}
			
			@Override
			public void onCancelled ( @NonNull DatabaseError databaseError ) {
				msgShort ( "Houve algum erro:" + databaseError );
			}
		} );
	}
	
	private void msgShort ( String msg ) {
		Toast.makeText ( getApplicationContext ( ), msg, Toast.LENGTH_SHORT ).show ( );
	}
	
	//comfirmar item com dialog
	private void confirmItem ( final int position, Product produtoSelecionado ) {
		
		AlertDialog.Builder alert = new AlertDialog.Builder ( this );
		alert.setTitle ( produtoSelecionado.getName ( ) );
		alert.setMessage ( "\nInforme a quantidade desejada: " );
		
		final EditText edtQuant = new EditText ( this );
		edtQuant.setText ( "1" );
		
		alert.setView ( edtQuant );
		alert.setPositiveButton ( "Confirmar", new DialogInterface.OnClickListener ( ) {
			@Override
			public void onClick ( DialogInterface dialog, int which ) {
				
				String quantity = edtQuant.getText ( ).toString ( );
				
				//trata erro de qt vazia
				if ( quantity.equals ( "" ) ) {
					quantity = "1";
					msgShort ( "Você não definiu Quantidade !" );
					msgShort ( "um item foi adicionado automaticamente." );
				}
				
				if ( validaQuantidade ( quantity ) == 0 ) {
					
					Product productSelectd = productsList.get ( position );
					OrderItens itemOrder = new OrderItens ( );
					
					
					itemOrder.setIdProduct ( productSelectd.getIdProd ( ) );
					itemOrder.setNameProduct ( productSelectd.getName ( ) );
					itemOrder.setItenSalePrice ( productSelectd.getSalePrice ( ) );
					itemOrder.setQuantity ( Integer.parseInt ( quantity ) );
					
					itensCars.add ( itemOrder );
					
					if ( ordersRecovery == null ) {
						ordersRecovery = new Orders ( retornIdUser );
					}
					ordersRecovery.setName ( user.getName ( ) );
					ordersRecovery.setAddress ( user.getAddress ( ) );
					ordersRecovery.setNeigthborhood ( user.getNeigthborhood ( ) );
					ordersRecovery.setNumberHome ( user.getNumberHome ( ) );
					ordersRecovery.setCellphone ( user.getPhone ( ) );
					
					ordersRecovery.setOrderItens ( itensCars );
					
					ordersRecovery.salvar ( );
				} else {
					edtQuant.setText ( "1" );
				}
			}
		} );
		
		alert.setNegativeButton ( "Cancelar", new DialogInterface.OnClickListener ( ) {
			@Override
			public void onClick ( DialogInterface dialog, int which ) {
			
			}
		} );
		AlertDialog dialog = alert.create ( );
		dialog.show ( );
	}
	
	private int validaQuantidade ( String valor ) {//valida se o valor digitado é numérico
		String regexStr = "^[0-9]*$";
		if ( !valor.trim ( ).matches ( regexStr ) ) {
			msgShort ( "Por favor, informe um valor numérico!" );
			return 1;
		} else return 0;
	}
	
	//recupera dados do usuario esta com
	private void recoveryDataUser ( ) {
		dialog = new SpotsDialog.Builder ( )
				.setContext ( this )
				.setMessage ( "Carregando dados aguarde...." )
				.setCancelable ( true )
				.build ( );
		dialog.show ( );
		
		DatabaseReference usuariosDB = reference.child ( "users" ).child ( retornIdUser );
		
		usuariosDB.addListenerForSingleValueEvent ( new ValueEventListener ( ) {
			@Override
			public void onDataChange ( DataSnapshot dataSnapshot ) {
				if ( dataSnapshot.getValue ( ) != null ) {
					user = dataSnapshot.getValue ( User.class );
				}
				//trata user null
				if ( ( user != null ) ) {
					// recupera pontos
					pontos = user.getPonts ( );
				}
				
				//se ponto 15 notifica
				if ( pontos == 15 ) {
					notificacaoPonto ( "Pontuação"
							, "Parabens você atingiu: " + user.getPonts ( )
							, "Faça o resgate na próxima compra" );
				}
				
				recoveryOrder ( );
			}
			
			@Override
			public void onCancelled ( DatabaseError databaseError ) {
			}
		} );
	}
	
	//recupera pedido
	private void recoveryOrder ( ) {
		DatabaseReference pedidoRef = reference
				.child ( "orders_user" )
				.child ( retornIdUser );
		
		pedidoRef.addValueEventListener ( new ValueEventListener ( ) {
			@Override
			public void onDataChange ( DataSnapshot dataSnapshot ) {
				qtdItensCar = 0;
				totalCar = 0.0;
				itensCars = new ArrayList<> ( );
				
				if ( dataSnapshot.getValue ( ) != null ) {
					ordersRecovery = dataSnapshot.getValue ( Orders.class );
					
					//trata null pointer apos
					// remover untimo iten carrinho
					if ( ordersRecovery != null ) {
						
						itensCars = ordersRecovery.getOrderItens ( );
						
					} else {
						Orders orders = new Orders ( );
						orders.removerOrderItens ( retornIdUser );
					}
					//trata NullPointer
					if ( itensCars != null ) {
						
						for ( OrderItens orderItens : itensCars ) {
							int qtde = orderItens.getQuantity ( );
							
							
							double preco = Double.parseDouble ( orderItens.getItenSalePrice ( ) );
							
							totalCar += ( qtde * preco );
							qtdItensCar += qtde;
						}
					} else {
						
						Orders orders = new Orders ( );
						orders.removerOrderItens ( retornIdUser );
					}
				}
				
				DecimalFormat df = new DecimalFormat ( "0.00" );
				
				txtQuantItens.setText ( String.valueOf ( qtdItensCar ) );
				txtTotalOrder.setText ( df.format ( totalCar ) );
				
				dialog.dismiss ( );
			}
			
			@Override
			public void onCancelled ( DatabaseError databaseError ) {
			
			}
		} );
	}
	
	//==> MENUS
	@Override
	public boolean onCreateOptionsMenu ( Menu menu ) {
		getMenuInflater ( ).inflate ( R.menu.menu_promotion, menu );
		return true;
	}
	
	@TargetApi ( Build.VERSION_CODES.LOLLIPOP )
	@RequiresApi ( api = Build.VERSION_CODES.LOLLIPOP )
	@Override
	public boolean onOptionsItemSelected ( MenuItem item ) {
		int id = item.getItemId ( );
		
		if ( id == R.id.menu_enter ) {
			Intent it = new Intent ( this, ActSaleCardap.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_plat_hot ) {
			Intent it = new Intent ( this, ActPlatHot.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_plat_ace ) {
			Intent it = new Intent ( this, ActPlatAce.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_combo ) {
			Intent it = new Intent ( this, ActCombo.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_drinks ) {
			Intent it = new Intent ( this, ActDrinks.class );
			startActivity ( it );
			return true;
		}
		if ( id == R.id.menu_temakis ) {
			Intent it = new Intent ( this, ActTemakis.class );
			startActivity ( it );
			return true;
		}
		if ( id == R.id.menu_edit_cadastro ) {
			Intent it = new Intent ( this, ActSignup.class );
			startActivity ( it );
			return true;
		}
		if ( id == R.id.menu_portions ) {
			Intent it = new Intent ( this, ActPortions.class );
			startActivity ( it );
			return true;
		}
		if ( id == R.id.menu_points ) {
			Intent it = new Intent ( this, ActPoints.class );
			startActivity ( it );
			return true;
		}
		if ( id == R.id.menu_satus ) {
			Intent it = new Intent ( this, ActWait.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_edit_privacy_policy ) {
			Intent it = new Intent ( this, ActPolicyPrivacy.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_edit_about ) {
			Intent it = new Intent ( this, ActInfo.class );
			startActivity ( it );
			return true;
		}
		
		if ( id == R.id.menu_addional ) {
			Intent it = new Intent ( this, ActAdditional.class );
			startActivity ( it );
			return true;
		}
		
		return super.onOptionsItemSelected ( item );
	}
	
	private void getDate ( ) {
		SimpleDateFormat dateFormat_hora = new SimpleDateFormat ( "HHmm" );
		
		Calendar cal = Calendar.getInstance ( );
		Date data_atual = cal.getTime ( );
		
		String hora_atual = dateFormat_hora.format ( data_atual );
		int intHora = Integer.parseInt ( hora_atual );
		
		if ( intHora > 1530 && intHora < 2300 ) {
			STATUS = getString ( R.string.we_are_open_now );
		} else {
			STATUS = getString ( R.string.we_are_not_open );
		}
	}
	
	//==>FIM MENUS
	private void notificacaoPonto ( String ticker, String titulo, String msg ) {
		
		
		NotificationManager nm = ( NotificationManager ) getSystemService ( NOTIFICATION_SERVICE );
		//PendingIntent p = PendingIntent.getActivity ( this, 0, new Intent ( this, ActPoints.class ), 0 );
		PendingIntent p = PendingIntent.getActivity ( this, 0, new Intent ( ), 0 );
		
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder ( this );
		builder.setTicker ( ticker );
		builder.setContentTitle ( titulo );
		
		builder.setSmallIcon ( R.mipmap.ic_launcher );
		builder.setLargeIcon ( BitmapFactory.decodeResource ( getResources ( ), R.mipmap.ic_launcher ) );
		builder.setContentIntent ( p );
		
		NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle ( );
		String[] descs = new String[] { msg };
		for ( int i = 0; i < descs.length; i++ ) {
			style.addLine ( descs[ i ] );
		}
		builder.setStyle ( style );
		
		Notification no = builder.build ( );
		no.vibrate = new long[] { 150, 300, 150 };
		no.flags = Notification.FLAG_AUTO_CANCEL;
		nm.notify ( R.mipmap.ic_launcher, no );
		
		try {
			Uri som = RingtoneManager.getDefaultUri ( RingtoneManager.TYPE_NOTIFICATION );
			Ringtone toque = RingtoneManager.getRingtone ( this, som );
			toque.play ( );
		} catch ( Exception e ) {
			
			System.out.println ( "Erro ao gerar toque notificação: " + e );
		}
	}
	
	public void listesnerEventPedidos ( ) {
		
		//retorna pedido
		DatabaseReference pedidosDB = reference.child ( "orders" );
		//recupara pedidos do user limitando  por id
		Query querySearch = pedidosDB.orderByChild ( "idUser" ).equalTo ( retornIdUser );
		
		
		querySearch.addChildEventListener ( new ChildEventListener ( ) {
			@Override
			public void onChildAdded ( @NonNull DataSnapshot dataSnapshot, @Nullable String s ) {
			}
			
			@Override
			public void onChildChanged ( @NonNull DataSnapshot dataSnapshot, @Nullable String s ) {
				// qualquer mudança de status sera alertada
				Orders orders = dataSnapshot.getValue ( Orders.class );
				
				notificacaoPonto ( "Status do Pedido"
						, "Status atual:" + orders.getStatus ( )
						, "O status mudou confira." );
				
			}
			
			@Override
			public void onChildRemoved ( @NonNull DataSnapshot dataSnapshot ) {
			}
			
			@Override
			public void onChildMoved ( @NonNull DataSnapshot dataSnapshot, @Nullable String s ) {
			}
			
			@Override
			public void onCancelled ( @NonNull DatabaseError databaseError ) {
				msgShort ( "Status Erro " + databaseError );
			}
		} );
		
	}
	
}
