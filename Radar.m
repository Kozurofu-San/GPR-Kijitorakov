close all; clear; clc
dev = serialport("COM4",9600,"Timeout",60);

CMD_FREQUENCY = 3;
CMD_SWEEP_PARAMETERS = 6;
CMD_SWEEP = 9;
CMD_OSC_SELECT = 12;
CMD_MIX_SELECT = 15;

fmin = 1000; % MHz
fmax = 3000; % MHz
fstep = 10; % MHz
texp = 60; % us
power = 3;
phase = 0;

n = (fmax-fmin)/fstep;
f = fmin:fstep:fmax-1;
Llow = 0.15;
Lhigh = 0.01;
dL = (Lhigh-Llow)/n;
L = (Llow:dL:Lhigh-dL);
dphaA = 2*pi*L./(3e8./(f*1e6));

ifmin = fmin/fstep+1;
ifmax = fmax/fstep+1;
fi = 0:fstep:fmax-1+ifmin*fstep;
ni = length(fi);
S = zeros(1,ni);

N = 1024*2;
dt = 1/fstep/N;
t = 0:dt:(N-1)*dt;
r = 3e2*t/2-0.9;

nw = 200;
w = zeros(nw,N);
dw = w;


windowi = 0.5*(1-cos(2*pi*(0:ni-1)/ni/2+pi));
% window = gausswin(n)';

fig = figure('Name','GPR','NumberTitle','off',...
    'Units','normalized','OuterPosition',[0 0.05 1 0.95]);

subplot(3,2,5)
hFre = plot(f,zeros(1,n),'b','LineWidth',1.2);
hold on
hFim = plot(f,zeros(1,n),'g');
hold off
grid on
title('Frequency')
% legend('Спектр','АС','ФС')
xlim([0 fmax])
xlabel('МГц')

subplot(3,2,1)
hFmag = plot(f,zeros(1,n),'r','LineWidth',1.2);
grid on
xlim([fmin fmax])
xlabel('МГц')
ylabel('Амплитуда, дБ')

subplot(3,2,3)
hFpha = plot(f,zeros(1,n),'m','LineWidth',1.2);
grid on
xlim([fmin fmax])
xlabel('МГц')
ylabel('Фаза, градусы')

subplot(2,2,2)
hTre = plot(r,zeros(1,N),'r');
hold on
hTim = plot(r,zeros(1,N),'g');
hold off
grid on
title('Time')
xlim([r(1) r(end)/6])
xlabel('м')

subplot(2,2,4)
hW = imagesc(r,0:nw-1,w);
colormap gray
xlim([r(1) r(end)/6])
xlabel('м')
title('Waterfall')

flush(dev)

write(dev,[CMD_SWEEP_PARAMETERS 0],"int8")
read(dev,3,'char')
write(dev,[fmin fmax fstep texp power phase],'int16')
read(dev,3,'char')

set(findall(gcf,'type','text'), 'FontSize', 10, 'Color', 'k','FontName', 'times')

while(1)
write(dev,[CMD_SWEEP 0],"int8")
data = read(dev,n*2+1,'int16');
mag = data(1:2:end-1)/2048;
hFmag.YData = mag*30;
mag = 10.^(30*mag/20);
mag = mag.^0.125;
% mag = 1;
pha = data(2:2:end-1)/2048;
hFpha.YData = pha*90;
% S = mag.*sin(pi*pha/2);
Sp = sin(pi/2*pha);
hil = 0;
if hil==0
    Sp = hilbert(Sp);
elseif hil==1
Sp = hilbert(Sp,N);
Sp = Sp(1:n);
else
% Hilbert transform
Sh = fft(Sp,N);
for i=2:N/2
    Sh(i) = 2*Sh(i);
end
for i=N/2+2:N
    Sh(i) = 0;
end
Sh = ifft(Sh,N);
Sp = real(Sp)+1j*imag(Sh(1:n));
end
% Sp(abs(Sp)==0) = 1;
% Sp = Sp./abs(Sp).*mag;
Spha = atan2(imag(Sp),real(Sp));
% Spha = Spha-4*dphaA;
Spha = mag.*(cos(Spha)+1j*sin(Spha));
% Spha = mag.*Sp;
% Spha = conv(Spha,Spha,"same");
% Spha = Spha.*window;
Spha = Spha-mean(Spha);
% window = 0.5*(1-cos(2*pi*(0:n-1)/n/2+pi));
% window = gausswin(n)';
window = chebwin(n)';
% window = blackmanharris(n)';
% windowi = blackmanharris(ni)'.^4;
Spha = Spha.*window;
% Spha = circshift(Spha,ifmin);
S(:) = 0;
S(ifmin:ifmax-1) = Spha;
S(1:length(Spha)) = Spha;
S = real(S);
% S = Spha;
% S = conv(S,diff(S),"same"); S = S.*exp(-2j*pi*(f*1e6)*1e-9); S =
% S-mean(S);
% S = S.*windowi;

locks = data(end);
hFre.YData = real(Sp);
hFim.YData = imag(Sp);
s = fft(S,N);
% s = s.*conj(s);
hTre.YData = real(s);
hTim.YData = abs(s);
w(2:end,:) = w(1:end-1,:);
w(1,:) = real(s);
dw = (w(1:end-1,:)-w(2:end,:));
dw = dw./max(dw,[],2);
hW.CData = w;
drawnow
end
