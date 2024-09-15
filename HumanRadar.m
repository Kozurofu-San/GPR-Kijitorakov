close all; clear; clc
dev = serialport("COM4",9600,"Timeout",60);

CMD_FREQUENCY = 3;
CMD_SWEEP_PARAMETERS = 6;
CMD_SWEEP = 9;
CMD_OSC_SELECT = 12;
CMD_MIX_SELECT = 15;

fmin = 600; % MHz
fmax = 3000; % MHz
fstep = 5; % MHz
texp = 60; % us
power = 3;
phase = 0;

n = (fmax-fmin)/fstep;
f = fmin:fstep:fmax-1;
Llow = 0.15;
Lhigh = 0.01;
dL = (Lhigh-Llow)/n;
L = (Llow:dL:Lhigh-dL);
dphaA = 2*2*pi*L./(3e8./(f*1e6));

ifmin = fmin/fstep+1;
ifmax = fmax/fstep+1;
fi = 0:fstep:fmax-1+ifmin*fstep;
ni = length(fi);
S = zeros(1,ni);

N = 4096;
dt = 1/fstep/N;
t = 0:dt:(N-1)*dt;
r = 3e2*t/2;

nw = 200;
w = zeros(nw,N);
dw = w;
dp = w;
wS = zeros(nw,n);

windowi = 0.5*(1-cos(2*pi*(0:ni-1)/ni/2+pi));
% window = gausswin(n)';

fig = figure('Name','GPR','NumberTitle','off',...
    'Units','normalized','OuterPosition',[0 0.05 1 0.95]);

subplot(2,2,1)
hF = plot(fi,zeros(1,ni));
hold on
hFmag = plot(f,zeros(1,n));
hFpha = plot(f,zeros(1,n));
hold off
grid on
title('Frequency')
legend('specter','mag','pha')
% xlim([0 fmax-1])
xlabel('MHz')

subplot(2,2,3)
hTre = plot(r,zeros(1,N));
hold on
hTim = plot(r,zeros(1,N));
hold off
grid on
title('Time')
xlim([0 r(end)/4])
xlabel('m')

subplot(2,2,2)
hW = imagesc(r,0:nw-1,w);
colormap gray
xlim([0 r(end)/4])
xlabel('m')
title('Waterfall')

subplot(2,2,4)
hP = imagesc(r,0:nw-1,w);
colormap gray
xlim([0 r(end)/4])
xlabel('m')
title('Processed')

flush(dev)

write(dev,[CMD_SWEEP_PARAMETERS 0],"int8")
read(dev,3,'char')
write(dev,[fmin fmax fstep texp power phase],'int16')
read(dev,3,'char')

while(1)
write(dev,[CMD_SWEEP 0],"int8")
data = read(dev,n*2+1,'int16');
mag = data(1:2:end-1)/2048;
hFmag.YData = mag;
mag = 10.^(30*mag/20);
mag = mag.^0.125;
pha = data(2:2:end-1)/2048;
hFpha.YData = pha;
Sp = sin(pi/2*pha);
Sp = hilbert(Sp);
Spha = atan2(imag(Sp),real(Sp))-2*dphaA;
Spha = mag.*(cos(Spha)+1j*sin(Spha));
Spha = Spha-mean(Spha);
window = chebwin(n)';
Spha = Spha.*window;
S(:) = 0;
S(ifmin:ifmax-1) = Spha;

locks = data(end);
hF.YData = real(S);
s = fft(S,N);
% s = real(s);
hTre.YData = real(s);
w(2:end,:) = w(1:end-1,:);
w(1,:) = s;
dw = abs(w(1:end-1,:)-w(2:end,:));
dw = dw./max(dw,[],2);
hW.CData = abs(w);
hP.CData = (dw);
wS(2:end,:) = wS(1:end-1,:);
wS(1,:) = Spha;

drawnow
end