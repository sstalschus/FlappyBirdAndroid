package com.projeto.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class jogo extends ApplicationAdapter {

	// usado para renderizar imagens/formas dentro do jogo
	private SpriteBatch batch; //
	private Texture[] passaros;
	private Texture[] passarosFantasma;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	// formas para colisão
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	// pontuação
	Preferences salvarPontuacao;

	// lista fantasma
	private List<Float> listaTemporaria;
	private List<Float> listaposicaoFinalPassaro;
	private int i = 0;
	private int controleListas = 2;

	// atributos
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos = 400;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano;
	private int estadoJogo = 0;

	// ajustar camera
	private OrthographicCamera camera;
	private Viewport viewport; // regula a distância da camera
	private final float VIRTUAL_WIDTH = 720; // TAM DA VIZUALIZAÇÃO LARGURA
	private final float VIRTUAL_HEIGHT = 1280; // ALTURA

	// textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont melhorPotuacao;

	// sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	
	@Override
	public void create () {

		chamarTexturas();
		chamarObjetos();
	}

	@Override
	public void render () {

		// Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificaEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();

	}

	private void verificaEstadoJogo(){

		// evento de clique
		boolean toqueTela = Gdx.input.justTouched();


		if( estadoJogo == 0){

			if( toqueTela ) {

				gravidade = -20;
				estadoJogo = 1;
				somVoando.play();
			}

		}else if( estadoJogo == 1){
			//if(controleListas%2 == 0){
				posicaoFantasma();
			//}

			if( toqueTela ) {
				gravidade = -20;
				somVoando.play();
			}

			// movimentar o cano
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if( posicaoCanoHorizontal < - canoTopo.getWidth() ){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
			}

			// aplicar gravidade no pássaro
			if( posicaoInicialVerticalPassaro > 0 || toqueTela )
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;

		}else if( estadoJogo == 2){

			listaposicaoFinalPassaro.clear();
			listaposicaoFinalPassaro.addAll(listaTemporaria);
			listaTemporaria.clear();

			if( pontos > pontuacaoMaxima ){
				pontuacaoMaxima = pontos;
				salvarPontuacao.putInteger("pontuacaoMaxima", pontuacaoMaxima); // cria uma chave dentro do dispositivo do usuário para salvar um número inteiro
			}

			// para o pássaro cair
			posicaoInicialVerticalPassaro--;

			// toque para reiniciar o jogo apos morrer
			if( toqueTela ){

				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoCanoHorizontal = larguraDispositivo;
				controleListas++;

			}
		}


	}

	private void detectarColisoes(){

		circuloPassaro.set(
				50 + passaros[0].getWidth()/2, posicaoInicialVerticalPassaro + passaros[0].getHeight()/2, passaros[0].getWidth()/2
		);
		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		retanguloCanoBaixo.set(
				posicaoCanoHorizontal, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);
		// testar se o pássaro se sobrepos aos retangulos
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps( circuloPassaro, retanguloCanoBaixo);

		if( colidiuCanoCima || colidiuCanoBaixo){
			if( estadoJogo == 1 ){ // Para fazer com que o som seja executado uma única vez
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void desenharTexturas(){

		batch.setProjectionMatrix( camera.combined ); // passa as config de exibição da camera

		batch.begin(); // exibir imagem/forma dentro do app

		// desenha imagem dentro do jogo
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo );
		batch.draw( passaros[(int) variacao], 50, posicaoInicialVerticalPassaro );
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf( pontos ), larguraDispositivo/2, alturaDispositivo - 100 );

		if( /*controleListas%2 != 0*/  estadoJogo == 1 && !listaposicaoFinalPassaro.isEmpty()){

			batch.draw(passarosFantasma[ (int) variacao ], 0, listaposicaoFinalPassaro.get(i), 110 , 110);
			i++;
		} else if(estadoJogo == 1){
			batch.draw(passarosFantasma[ (int) variacao ], 0, 0, 110 , 110);
		}


		// desenhar textura de game over
		if( estadoJogo == 2 ){
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo/2 - 140 , alturaDispositivo/2 - gameOver.getHeight()/2);
			melhorPotuacao.draw(batch, "Seu record é: " + pontuacaoMaxima, larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight() );
		}

		batch.end(); // identificar que é o final do desenho no app

	}

	private void chamarTexturas(){

		passaros 	= new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		passarosFantasma = new Texture[3];
		passarosFantasma[0] = new Texture("passaro_f_dois.png");
		passarosFantasma[1] = new Texture("passaro_f_tres.png");
		passarosFantasma[2] = new Texture("passaro_f_um.png");

		fundo 			= new Texture("fundo.png");
		canoBaixo 		= new Texture("cano_baixo_maior.png");
		canoTopo		= new Texture("cano_topo_maior.png");
		gameOver		= new Texture("game_over.png");
	}

	private void chamarObjetos(){

		batch 	= new SpriteBatch(); // instanciando o Batch
		random	= new Random();

		larguraDispositivo =  VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo/2;
		posicaoCanoHorizontal = larguraDispositivo;

		// textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		melhorPotuacao = new BitmapFont();
		melhorPotuacao.setColor(Color.RED);
		melhorPotuacao.getData().setScale(2);

		// formas de colisões
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		// inicializar sons
		somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav"));

		// prefs
		salvarPontuacao = Gdx.app.getPreferences("flappybird"); // Com o preferences, se a pessoa fechar o app a pontuação continua salva no celular do usuário
		pontuacaoMaxima = salvarPontuacao.getInteger("pontuacaoMaxima", 0); // se n existe = 0

		// config camera
		camera = new OrthographicCamera();
		camera.position.set( VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0); // configurar a posição da camera
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera); // configura o viewport

		// list
		listaposicaoFinalPassaro = new ArrayList<>();
		listaTemporaria = new ArrayList<>();
	}

	private void validarPontos(){

		if( posicaoCanoHorizontal < 30 ){ // passou do passaro
			if(!passouCano) {
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
		variacao += Gdx.graphics.getDeltaTime() * 10;
		if (variacao > 3)
			variacao = 0;
	}

	@Override
	public void resize(int width, int height) { // metodo que recebe a largura e altura e atualiza
		viewport.update(width, height);
	}

	public void posicaoFantasma(){
		listaTemporaria.add(posicaoInicialVerticalPassaro);
		//listaposicaoFinalPassaro.add(posicaoInicialVerticalPassaro);
	}


	@Override
	public void dispose () {

	}
}
