% Переключающийся алгоритм обнаружения подвижных и малоподвижных объектов
% Всегда работает алгоритм обнаружения подвижных целей (Moving algorithm).
% До тех пор, пока у этого алгоритма нет обнаружений, работает алгоритм
% обнаружения малоподвижных целей Not moving algorithm - в скользящем окне
% вычисляется среднее,
% из него вычитается последний принятый кадр. Как только Moving algorithm
% обнаружил подвижную цель, начинается её траекторная обработка, а алгоритм
% Not moving приостанавливается. Результат - на графике Result

clear
close all
clc
%% Открытие файла с записью
Disc = strcat(pwd,filesep);
Content = struct2cell(dir(Disc)).';
DataFiles = "";
t = 1;
for f = 1:size(Content,1)
    File = cell2mat(strfind((Content(f,1)),'.mat'));
    if ~isempty(File)
        t = t+1;
        DataFile = char(Content(f,1));
        DataFiles(t,:) = string(DataFile(1:File-1));
    end
end

%% Коэффициенты
Samp = 480;	% Количество частот СЧМ
FFT = 512;		% Количество отсчётов по дальности после ДПФ
%Range = (0:FFT-1)*30/(FFT-1);
Range = (1:FFT);
WF = 200;	% Waterfall size

%% Графики
fig = figure('Name', 'Графики', 'NumberTitle', 'off',...
    'Units', 'normalized', 'OuterPosition', [0 0.05 1 0.95]);

axE = subplot(2,2,3);
hE = imagesc(zeros(WF,FFT));
set(gca,'YDir','r')
colormap(gca,'jet')
xlim([1 FFT/2])
title('Result')

subplot(2,2,1)
hF = plot(NaN,NaN);
hold on
hF1 = plot(NaN,NaN);
hF2 = gobjects(1,10);
for i=1:10
hF2(i) = plot(NaN,NaN,'LineStyle','none','MarkerSize',5,...
'Marker','o','MarkerEdgeColor','k','MarkerFaceColor','r');
end
hold off
grid on
grid minor
xlim([0 Range(FFT)])
title('Moving algorithm')

axB = subplot(2,2,2);
hB = imagesc();
set(gca,'YDir','r')
colormap(gca,'hot')
xlim([1 FFT/2])
title('Not moving algorithm')

subplot(2,2,4)
hA = plot(NaN,NaN);
hold on
hAA = plot(NaN,NaN);
hAAA = plot(NaN,NaN);
hold off
grid on
grid minor
xlim([0 Range(FFT)])
title('Not moving estimation')

list = uicontrol(fig,'Style','popupmenu');
list.Position = [10 575 140 20];
list.String = DataFiles;

take = 0;
while(1)
take = take+1;
waitfor(list,'Value')
CurFile = DataFiles(list.Value);
Path = strcat(Disc, CurFile,'.mat');
% Input = fread(fopen(Path,'r'),'int32','b')';   % Входные данные
load(Path)
Data = wS;
% fclose all;
% Data = complex(Input(1:2:end),Input(2:2:end));
% Data = reshape(Data,Samp,[]).';

%% Алгоритм для неподвижных объектов
% Преобразование Фурье от исходных данных
% Data[450,350] - исходный массив отсчётов после обработки СЧМ сигнала
% 350 импульсов
% 450 отсчётов времени зондирования
Data = Data.*hamming(Samp)';		% Hamming window
Duration = size(Data,1);		% Number of frames

% Реестр целей. В каждой строке информация о цели. Индексы реестра:
L_Num = 1;	% Num - количество наблюдений (кадров)
L_W = 2;	% W - вес цели
L_Det = 3;	% 10 обнаружений
L_x = 13;	% Текущие координаты. X - история перемещения цели
L_x_appr = 43;	% Аппроксимированные координаты
L_Stat = 44;	% Статус: 1 - движение

% Реестр целей. В каждой строке информация о цели. Индексы реестра:
Ln_Num = 1;	% Num - количество наблюдений (кадров)
Ln_W = 2;	% W - вес цели
Ln_x = 3;	% Текущие координаты
Ln_Stat = 4;	% Статус: 1 - стояние

% Constants values
WinX = 10;	% элементов - зона вокруг человека
Wmax = 30;	% Max weight
Wdet = 20;	% Detection weight
SmoothFrames = 5;	% Smooth size
Buffersize = 50;	% Size of circular buffer for FFT result
SmWin = gausswin(10);	% Smoothing function
MaxTar = 20;		% Max number of targets

% Allocate in RAM
FFTData = zeros(SmoothFrames,FFT);% Sliding window accumulates frames
SmoothData = zeros(1,FFT);	% Smoothing window
TimeVector = zeros(1,FFT);	% Vector after FFT
TimeData = zeros(Buffersize,FFT);	% Array after Fourier transform
CFARWin = [ones(1,10) zeros(1,15) ones(1,10)];		% Smoothing function
PrMarks = zeros(1,MaxTar);	% Prime marks array
Reg = zeros(MaxTar,L_Stat);	% Target registry
Regn = zeros(MaxTar,Ln_Stat);	% Target registry
SignalWF = zeros(WF,FFT);
LastTar = 1;
LastTarn = 1;

%%
% RT continuus implementation
for fr = 1:Duration		% Real time framing
Input = Data(fr,:);		% Input obtained vector
TimeVector = (fft(Input,FFT))/FFT;	% Fourier transform
TimeData(2:end,:) = TimeData(1:end-1,:);
TimeData(1,:) = TimeVector;	% Write to array
if fr<2
continue	% Store frames at least
end

%% Check the moving human
% Primary marks forming
SmoothData(:,:) = abs(TimeData(2,:)-TimeData(1,:));	% First frame - differencial signal
FFTData(2:end,:) = FFTData(1:end-1,:);	% Circular buffer
FFTData(1,:) = SmoothData;		% New frame
if fr<SmoothFrames+1
continue	% Store frames at least
end
Signal = mean(FFTData,1);	% Smooth
Signal(1:20) = 0;
Signal = conv(Signal,SmWin,'same')/sum(SmWin);	% Smoothing along range
% Signal = conv(Signal,SmWin,'same')/sum(SmWin);	% Smoothing along range
Thres1 = repelem(5*median(Signal),1,FFT);	% Back as thseshold
Thres = 1*conv(Signal,CFARWin,'same')/sum(CFARWin)+Thres1;
		SignalWF(2:end) = SignalWF(1:end-1);
		SignalWF(1,:) = Signal/max(Signal)*0.7;
		set(hF,'XData',Range,'YData',Signal)
		set(hF1,'XData',Range,'YData',Thres)
		set(hE,'CData',SignalWF)
%         set(hB,'CData',SignalDiff)	
		drawnow
Alarm = Signal-Thres;		% Signal overaboundance
Alarm(Alarm<0) = 0;		% Only positive values
PrMarks(:) = 0;
[~,Detect] = findpeaks(Alarm);	% Primary detections
LastMark = length(Detect);	% Index of last primary mark
if ~isempty(Detect)
PrMarks(1:LastMark) = Detect;	% Add new marks
end

%% Secondary marks processing
LastMark(LastMark>MaxTar) = MaxTar;	% Limit
LastTar(LastTar==0) = 1;	% There is no targets at beginning
Reg(:,L_Det+1:L_Det+9) = Reg(:,L_Det:L_Det+8);	% Detections shift
Reg(:,L_Det) = 0;	% Null current detections
Reg(:,L_x+1:L_x+29) = Reg(:,L_x:L_x+28);	% Story shift

for tar = 1:LastTar
	W = Reg(tar,L_W);	% Previous weight
	x_cur = Reg(tar,L_x);	% Previous coordinates
	winx = [x_cur-WinX x_cur+WinX];	% Displacement window
	winx = round(winx);
	winx(winx<1) = 1;	% Window limitation
	winx(winx>FFT) = FFT;
	[Amp,x] = max(Alarm(winx(1):winx(2)));	% Is there new peak?
	if Amp>0		% If signal overcomes the threshold
		[Amp,x] = max(Signal(winx(1):winx(2)));	% Maxima
		Reg(tar,L_x) = x+winx(1)-1;	% New coordinates
		Reg(tar,L_Det) = 1;	% Detection
		W = W+1;	% Increase weight
	elseif Amp==0		% If signal is lower than the threshold
		W = W-1;	% Decrease weight
	end
	W(W>Wmax) = Wmax;	% Limit weight
	W(W<0) = 0;
	Reg(tar,L_W) = W;	% Write weight
	Reg(tar,L_Num) = Reg(tar,L_Num)+1;	% How many frames does target escort
end

	% Add new targets. Scan all targets to find matches
	inew = 0;	% New targets array
	for tar1 = 1:LastMark
		cnt = 0;	% Match counter
		for tar2 = 1:LastTar
			% Find matches in bound of window
			if(PrMarks(tar1)>=Reg(tar2,L_x)-WinX...
				&& PrMarks(tar1)<=Reg(tar2,L_x)+WinX)
			cnt = cnt+1; % Increase counter if there is match
			end
		end
		if ~cnt		% If there in no matches, then it is new target
			inew = inew+1;	% Index
			if inew>MaxTar	% If array overflows
				break	% Finish adding
			end
			Reg(LastTar+inew,L_x) = PrMarks(tar1);	% Wrine new x
			Reg(LastTar+inew,L_Num) = 1;	% First appearance
		end
	end
	LastTar = LastTar+inew;		% New last target
	LastTar(LastTar>20) = 20;
	
	% Repeating targets elimination
	tar1 = 1;	% Target pointer
	while tar1<LastTar
		for tar2 = 1:LastTar
			if tar1~=tar2	% Itself
			% Find matches in bound of window
			if(Reg(tar1,L_x)>=Reg(tar2,L_x)-WinX...
				&& Reg(tar1,L_x)<=Reg(tar2,L_x)+WinX)
			Reg(tar2,:) = 0;	% Clear target information
			Reg(tar2:LastTar-1,:) = Reg(tar2+1:LastTar,:);	% Shift
			LastTar = LastTar-1;	% Decrease target pointer
			end
			end
		end
	tar1 = tar1+1;	% Next pointer
	end
	
	% False marks annihilation
	tar1 = 1;		% Target pointer
	while tar1<=LastTar
	if(Reg(tar1,L_Num)>30 && Reg(tar1,L_W) == 0)	% After 30 observations
	if sum(Reg(tar1,L_Det:L_Det+9),2)<3	% Sum of detctions less than 3
		Reg(tar1,:) = 0;		% Clear target information
		Reg(tar1:LastTar-1,:) = Reg(tar1+1:LastTar,:);	% Shift
		LastTar = LastTar-1;		% Decrease target pointer
	end
	end
	tar1 = tar1+1;		% Next target
	end
	
	% Linear approximation y = ax+b. x<=arg(1..30), y<=x(value)
	% a = (Nsum(xy)-sum(x)sum(y))/(Nsum(x^2)-sum(x)^2)
	% b = (sum(y)-asum(x))/N
	
	for tar1 = 1:LastTar
		N = Reg(tar1,L_Num);	% Number of observation
		N(N>30) = 30;		% Limit
		if N>20			% Enough number of observations
		sumx = sum(0:N-1);	% Sum of arguments
		sumx2 = sum((0:N-1).^2);	% Sum of arguments squares
		sumy = sum(Reg(tar1,L_x:L_x+N-1));	% Sum of values
		sumxy = sum(Reg(tar1,L_x:L_x+N-1).*(0:N-1));	% Sum of production
		a = (N*sumxy-sumx*sumy)/(N*sumx2-sumx^2);
		b = (sumy-a*sumx)/N;
		x0_appr = a*0+b;
		xN_appr = a*N+b;
		Reg(tar1,L_x_appr) = round(x0_appr);		% Predicted Value
		if ~Reg(tar1,L_Det)
			Reg(tar1,L_x) = Reg(tar1,L_x_appr);
		end
		if abs(x0_appr-xN_appr)>10 && N>20	% For fast target
		Reg(tar1,L_Stat) = 1;		% Moving status
		end
		end
	end
		% Indication
		tar2 = 0;	% Figure index
		for tar1 = 1:LastTar
		if Reg(tar1,L_Stat)	% Fro moving targets
		tar2 = tar2+1;	% Increase figure index
		set(hF2(tar2),'XData',Reg(tar1,L_x_appr),'YData',0)
		w = [Reg(tar1,L_x_appr)-1 Reg(tar1,L_x_appr)+1];
		w(w<1)=1; w(w>FFT)=FFT;
		SignalWF(1,w(1):w(2)) = 1;% Draw dot
		end
		end
		set(hE,'CData',SignalWF)
		set(hF2(tar2+1:10),'XData',NaN,'YData',NaN)	% Clear ramaining dots
		drawnow
	
	% Resort registry by moving status and weight in descend order
	[Reg,~] = sortrows(Reg,[L_Stat L_W],'descend');

if fr>Buffersize	% Store frames at least
%% Roman's not moving human detection algorithm
if ~Reg(:,L_Stat)	% If there is no moving target
Signal = TimeData;	% Input signal
% Фон вычисляется после наблюдения
t = 1/5;	% Фон выбираем по этому отношению
Back = sort(Signal,1);	% Сортировать значения на каждой дальности по времени
Back = Back(round(t*Buffersize),:);				% Берём ~1/5 значение. Т. е. Ч должен
% присутствовать на этой дальности >1/5 от времени наблюдения
DifferenceW = abs(Signal-Back);	% Вычитаем фон
DifferenceW([1:2 Buffersize-2:Buffersize],:) = 0;	% Сигнал в это время = 0, а фон есть, поэтому вырезаем
DifferenceW(:,480:512) = 0;		% Вырезаем пролаз
% 		set(hB,'CData',DifferenceW)	
% Вердикт
Final = mean(DifferenceW,1);	% Обрабатываем средний сигнал
Final([1:20 450:512]) = 0;
win = 2;	% Окно сглаживания
Smooth = zeros(1,512);		% Сглаженный сигнал
for r = win/2+1:512-win/2	% По всем дальностям, кроме крайних
	Smooth(r) = mean(Final(r-win/2:r+win/2));	% Сглаживание нужно
	% для выделения человека, так как от него отклик шире, чем от отражателей,
	% это типа корреляция
end

Smooth = conv(Smooth,SmWin,'same')/sum(SmWin);
Thres1 = 1*mean(Smooth);		% Порог - константа, основанный на среднем уровне фона
Thres1 = repelem(Thres1,1,512);
Thres = conv(Smooth,CFARWin,'same')/sum(CFARWin)+Thres1;
Alarmn = Smooth-Thres;		% Ищем превышение порога
Alarmn(Alarmn<0) = 0;
[~,Detect] = findpeaks(Alarmn);
LastMark = length(Detect);
% 		set(hA,'XData',Range,'YData',Final)
		set(hAA,'XData',Range,'YData',Smooth)
		set(hAAA,'XData',Range,'YData',Thres)

LastMark(LastMark>MaxTar) = MaxTar;	% Limit
LastTarn(LastTarn==0) = 1;	% There is no targets at beginning
for tar = 1:LastTarn
	W = Regn(tar,Ln_W);	% Previous weight
	x_cur = Regn(tar,Ln_x);	% Previous coordinates
	winx = [x_cur-10 x_cur+10];	% Displacement window
	winx = round(winx);
	winx(winx<1) = 1;	% Window limitation
	winx(winx>FFT) = FFT;
	[Amp,x] = max(Alarmn(winx(1):winx(2)));	% Is there new peak?
	if Amp>0		% If signal overcomes the threshold
		[Amp,x] = max(Smooth(winx(1):winx(2)));	% Maxima
		Regn(tar,Ln_x) = x+winx(1)-1;	% New coordinates
		W = W+1;	% Increase weight
	elseif Amp==0		% If signal is lower than the threshold
		W = W-1;	% Decrease weight
	end
	W(W>Wmax) = Wmax;	% Limit weight
	W(W<0) = 0;
	Regn(tar,Ln_W) = W;	% Write weight
	Regn(tar,Ln_Num) = Regn(tar,Ln_Num)+1;	% How many frames does target escort
end
	% Add new targets. Scan all targets to find matches
	inew = 0;	% New targets array
	for tar1 = 1:LastMark
		cnt = 0;	% Match counter
		for tar2 = 1:LastTarn
			% Find matches in bound of window
			if(Detect(tar1)>=Regn(tar2,Ln_x)-WinX...
				&& Detect(tar1)<=Regn(tar2,Ln_x)+WinX)
			cnt = cnt+1; % Increase counter if there is match
			end
		end
		if ~cnt		% If there in no matches, then it is new target
			inew = inew+1;	% Index
			if inew>MaxTar	% If array overflows
				break	% Finish adding
			end
			Regn(LastTarn+inew,Ln_x) = Detect(tar1);	% Wrine new x
			Regn(LastTarn+inew,Ln_Num) = 1;	% First appearance
		end
	end
	LastTarn = LastTarn+inew;		% New last target
	LastTarn(LastTarn>20) = 20;
	
	% Repeating targets elimination
	tar1 = 1;	% Target pointer
	while tar1<LastTarn
		for tar2 = 1:LastTarn
			if tar1~=tar2	% Itself
			% Find matches in bound of window
			if(Regn(tar1,Ln_x)>=Regn(tar2,Ln_x)-WinX...
				&& Regn(tar1,Ln_x)<=Regn(tar2,Ln_x)+WinX)
			Regn(tar2,:) = 0;	% Clear target information
			Regn(tar2:LastTarn-1,:) = Regn(tar2+1:LastTarn,:);	% Shift
			LastTarn = LastTarn-1;	% Decrease target pointer
			end
			end
		end
	tar1 = tar1+1;	% Next pointer
	end
	
	% False marks annihilation
	tar1 = 1;		% Target pointer
	while tar1<=LastTarn
	if(Regn(tar1,Ln_Num)>30 && Regn(tar1,L_W) == 0)	% After 30 observations
		Regn(tar1,:) = 0;		% Clear target information
		Regn(tar1:LastTarn-1,:) = Regn(tar1+1:LastTarn,:);	% Shift
		LastTarn = LastTarn-1;		% Decrease target pointer
	end
	tar1 = tar1+1;	% Next target
	end
	
	% Verdict
	
	for tar1 = 1:LastTarn
	if ~Regn(tar1,Ln_Stat)
	if Regn(tar1,Ln_W)>15
	Regn(tar1,Ln_Stat) = 1;
	end
	end
	end
	
		% Indication
		for tar1 = 1:LastTarn
		if Regn(tar1,Ln_Stat)
		w = [Regn(tar1,Ln_x)-1 Regn(tar1,Ln_x)+1];
		w(w<1)=1; w(w>FFT)=FFT;
		SignalWF(1,w(1):w(2),1) = 1;
		else
		end
		end
		set(hE,'CData',SignalWF)
		drawnow
end		
end

end
end