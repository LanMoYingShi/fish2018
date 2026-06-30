package com.fongmi.android.tv.player.karaoke;

public class KaraokeScorer {

    private final KaraokeTrack track;
    private final KaraokeScoringConfig config;
    private long lastPositionMs = -1;
    private double totalWeightMs;
    private double hitWeightMs;
    private double voicedWeightMs;
    private long currentComboMs;
    private long bestComboMs;
    private final double[] lineTotalWeightMs;
    private final double[] lineHitWeightMs;
    private KaraokeScoreSnapshot snapshot;

    public KaraokeScorer(KaraokeTrack track) {
        this(track, KaraokeScoringConfig.DEFAULT);
    }

    public KaraokeScorer(KaraokeTrack track, KaraokeScoringConfig config) {
        this.track = track == null ? new KaraokeTrack("", "", 0, 0, null) : track;
        this.config = config == null ? KaraokeScoringConfig.DEFAULT : config;
        int lineCount = Math.max(1, this.track.getScoredLineCount());
        this.lineTotalWeightMs = new double[lineCount];
        this.lineHitWeightMs = new double[lineCount];
        this.snapshot = new KaraokeScoreSnapshot(0, 0, null, Double.NaN, Double.NaN, false, false);
    }

    public KaraokeScoreSnapshot update(long positionMs, double frequencyHz, double volume, double confidence) {
        long adjustedPositionMs = Math.max(0, positionMs - config.getInputLatencyMs());
        Sample sample = sample(adjustedPositionMs, frequencyHz, volume, confidence);
        long sliceMs = nextSlice(adjustedPositionMs);
        if (sliceMs > 0 && sample.note != null && sample.note.isScored()) {
            double weight = sliceMs * sample.note.getType().getScoreWeight();
            totalWeightMs += weight;
            if (sample.voiced) voicedWeightMs += weight;
            if (sample.hit) hitWeightMs += weight;
            scoreLine(sample.note, weight, sample.hit);
            updateCombo(sliceMs, sample.hit);
        }
        lastPositionMs = adjustedPositionMs;
        return snapshot = snapshot(adjustedPositionMs, sample);
    }

    public KaraokeScoreSnapshot evaluate(long positionMs, double frequencyHz, double volume, double confidence) {
        long adjustedPositionMs = Math.max(0, positionMs - config.getInputLatencyMs());
        Sample sample = sample(adjustedPositionMs, frequencyHz, volume, confidence);
        return snapshot(adjustedPositionMs, sample);
    }

    public KaraokeScoreSnapshot getSnapshot() {
        return snapshot;
    }

    public void reset() {
        lastPositionMs = -1;
        totalWeightMs = 0;
        hitWeightMs = 0;
        voicedWeightMs = 0;
        currentComboMs = 0;
        bestComboMs = 0;
        for (int i = 0; i < lineTotalWeightMs.length; i++) {
            lineTotalWeightMs[i] = 0;
            lineHitWeightMs[i] = 0;
        }
        snapshot = new KaraokeScoreSnapshot(0, 0, null, Double.NaN, Double.NaN, false, false);
    }

    private void scoreLine(KaraokeNote note, double weight, boolean hit) {
        int index = lineIndex(note);
        lineTotalWeightMs[index] += weight;
        if (hit) lineHitWeightMs[index] += weight;
    }

    private void updateCombo(long sliceMs, boolean hit) {
        if (hit) {
            currentComboMs += sliceMs;
            bestComboMs = Math.max(bestComboMs, currentComboMs);
        } else {
            currentComboMs = 0;
        }
    }

    private KaraokeScoreSnapshot snapshot(long positionMs, Sample sample) {
        return new KaraokeScoreSnapshot(positionMs, totalWeightMs, hitWeightMs, voicedWeightMs, currentComboMs, bestComboMs, sample.note, sample.sungMidi, sample.distanceSemitones, sample.voiced, sample.hit, lineIndex(sample.note), lineTotalWeightMs.length, scoredLineCount(), currentLineScore(sample.note), bestLineScore(), averageLineScore());
    }

    private long nextSlice(long positionMs) {
        if (lastPositionMs < 0) return 0;
        long delta = positionMs - lastPositionMs;
        if (delta <= 0 || delta > 2_000) {
            currentComboMs = 0;
            return 0;
        }
        return Math.min(delta, config.getMaxSliceMs());
    }

    private Sample sample(long positionMs, double frequencyHz, double volume, double confidence) {
        KaraokeNote note = track.findScoredNote(positionMs);
        boolean voiced = frequencyHz > 0 && volume >= config.getMinVolume() && confidence >= config.getMinConfidence();
        double sungMidi = voiced ? KaraokePitch.frequencyToMidi(frequencyHz) : Double.NaN;
        double distance = note != null && voiced ? KaraokePitch.semitoneDistance(sungMidi, note.getPitch(), config.isIgnoreOctave()) : Double.NaN;
        boolean hit = note != null && voiced && (!note.isPitchRequired() || Math.abs(distance) <= config.getToleranceSemitones());
        return new Sample(note, sungMidi, distance, voiced, hit);
    }

    private int lineIndex(KaraokeNote note) {
        if (note == null) return -1;
        return Math.max(0, Math.min(lineTotalWeightMs.length - 1, note.getLineIndex()));
    }

    private int currentLineScore(KaraokeNote note) {
        int index = lineIndex(note);
        if (index < 0 || lineTotalWeightMs[index] <= 0) return 0;
        return percent(lineHitWeightMs[index], lineTotalWeightMs[index]);
    }

    private int bestLineScore() {
        int best = 0;
        for (int i = 0; i < lineTotalWeightMs.length; i++) if (lineTotalWeightMs[i] > 0) best = Math.max(best, percent(lineHitWeightMs[i], lineTotalWeightMs[i]));
        return best;
    }

    private int averageLineScore() {
        int count = 0;
        int total = 0;
        for (int i = 0; i < lineTotalWeightMs.length; i++) {
            if (lineTotalWeightMs[i] <= 0) continue;
            total += percent(lineHitWeightMs[i], lineTotalWeightMs[i]);
            count++;
        }
        return count == 0 ? 0 : Math.round(total / (float) count);
    }

    private int scoredLineCount() {
        int count = 0;
        for (double weight : lineTotalWeightMs) if (weight > 0) count++;
        return count;
    }

    private static int percent(double hit, double total) {
        if (total <= 0) return 0;
        return (int) Math.round(Math.max(0, Math.min(100, hit * 100.0 / total)));
    }

    private static class Sample {

        private final KaraokeNote note;
        private final double sungMidi;
        private final double distanceSemitones;
        private final boolean voiced;
        private final boolean hit;

        private Sample(KaraokeNote note, double sungMidi, double distanceSemitones, boolean voiced, boolean hit) {
            this.note = note;
            this.sungMidi = sungMidi;
            this.distanceSemitones = distanceSemitones;
            this.voiced = voiced;
            this.hit = hit;
        }
    }
}
